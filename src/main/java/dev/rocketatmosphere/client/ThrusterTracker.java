package dev.rocketatmosphere.client;

import dev.propulsionteam.propulsionsimulated.content.thruster.AbstractThrusterBlockEntity;
import dev.propulsionteam.propulsionsimulated.content.thruster.AbstractThrusterBlockEntity.WorldExhaustRay;
import dev.propulsionteam.propulsionsimulated.content.thruster.thruster.creative_thruster.CreativeThrusterBlockEntity.PlumeType;
import dev.rocketatmosphere.RocketAtmosphere;
import dev.rocketatmosphere.config.Settings;
import dev.rocketatmosphere.engine.RocketEngineBlockEntity;
import dev.rocketatmosphere.space.SpaceZone;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.level.Level;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * Ponto de entrada dos efeitos. Chamado 1x por tick por motor (via mixin em ThrusterSoundHooks.clientTick).
 * Trabalho barato: sai cedo para motores desligados/distantes; o raio de escape e re-lido a cada 2 ticks.
 */
public final class ThrusterTracker {
    private static final float MIN_THROTTLE = 0.02f;
    private static final int RAY_REFRESH_TICKS = 2;

    private static final Map<AbstractThrusterBlockEntity, ThrusterState> STATES = new WeakHashMap<>();

    private static Settings settings;
    private static long settingsTick = Long.MIN_VALUE;
    private static boolean hookSeen;
    private static boolean failed;
    private static int errors;

    private ThrusterTracker() {}

    public static Settings settings() {
        Settings s = settings;
        if (s == null) {
            s = Settings.read();
            settings = s;
        }
        return s;
    }

    /** Forca reler a config (usado pelo comando de qualidade). */
    public static void refreshSettings() {
        settings = Settings.read();
        settingsTick = Long.MIN_VALUE;
    }

    public static boolean hookSeen() { return hookSeen; }
    public static boolean failed() { return failed; }
    public static int trackedCount() { return STATES.size(); }

    public static int inSpaceCount() {
        int n = 0;
        for (ThrusterState s : STATES.values()) {
            if (s.space != null && s.space.zone == SpaceZone.SPACE) n++;
        }
        return n;
    }

    public static void onThrusterTick(AbstractThrusterBlockEntity be) {
        if (failed) return;
        try {
            handle(be);
        } catch (Throwable t) {
            // Efeitos visuais nunca podem derrubar o jogo.
            if (++errors >= 5) {
                failed = true;
                RocketAtmosphere.LOGGER.error("[RocketAtmosphere] efeitos desativados apos erros repetidos", t);
            } else {
                RocketAtmosphere.LOGGER.warn("[RocketAtmosphere] erro ao processar motor (" + errors + "/5)", t);
            }
        }
    }

    private static void handle(AbstractThrusterBlockEntity be) {
        Minecraft mc = Minecraft.getInstance();
        Level level = be.getLevel();
        LocalPlayer player = mc.player;
        if (level == null || player == null || level != mc.level) return;

        long now = level.getGameTime();
        if (settings == null || now - settingsTick >= 20 || now < settingsTick) {
            settings = Settings.read();
            settingsTick = now;
        }
        Settings cfg = settings;
        if (!cfg.enabled) return;

        if (!hookSeen) {
            hookSeen = true;
            RocketAtmosphere.LOGGER.info("[RocketAtmosphere] gancho ativo: primeiro motor do Create Propulsion detectado.");
        }

        if (be.isMultiblock() && !be.isController()) return;      // so o controlador do bloco 2x2/3x3

        float throttle = Math.min(1.0f, be.getEffectiveThrottle());
        if (throttle < MIN_THROTTLE || !be.isVisuallyActive()) {
            if (!STATES.isEmpty()) STATES.remove(be);
            return;
        }

        // Perfil do motor. Vector (FE), Ion e Creative Vector herdam do motor ionico no Propulsion
        // (isIon() == true): recebem o perfil "eletrico". Creative usa o tipo de pluma escolhido.
        boolean electric;
        if (be.isCreative()) {
            PlumeType type = be.getPlumeType();
            if (type == PlumeType.NONE) {
                if (!STATES.isEmpty()) STATES.remove(be);
                return;
            }
            electric = type != PlumeType.PLUME;
        } else {
            electric = be.isIon();
        }
        if (electric && !cfg.electric) {
            if (!STATES.isEmpty()) STATES.remove(be);
            return;
        }

        ThrusterState st = STATES.computeIfAbsent(be, k -> new ThrusterState());

        if (now >= st.nextRay) {
            WorldExhaustRay ray = be.getWorldExhaustRay();
            if (ray == null) {
                st.valid = false;
                return;
            }
            st.updateRay(ray.nozzlePos(), ray.direction(), now);
            st.nextRay = now + RAY_REFRESH_TICKS;
        }
        if (!st.valid) return;

        double dx = st.nozzle.x - player.getX();
        double dy = st.nozzle.y - player.getY();
        double dz = st.nozzle.z - player.getZ();
        double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);

        // Camera shake: acumula a contribuicao deste motor neste tick (motores maiores pesam mais).
        if (cfg.shake && dist < cfg.shakeRange) {
            double size = (1.0 + 0.35 * Math.max(0, be.width - 1)) * (electric ? 0.55 : 1.0);
            CameraShake.addSource(throttle * size, dist, cfg.shakeRange);
        }

        if (dist > cfg.maxDistance) return;                       // corte por distancia
        if (now < st.nextEmit) return;                            // espacamento por qualidade
        st.nextEmit = now + cfg.emitInterval;

        double smoke = be instanceof RocketEngineBlockEntity engine ? engine.smokeFactor() : 1.0;
        ThrusterEffects.emit(level, cfg, st, throttle, dist, now, electric, smoke);
    }
}

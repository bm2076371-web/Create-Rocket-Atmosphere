package dev.rocketatmosphere.client;

import dev.rocketatmosphere.RocketAtmosphere;
import dev.rocketatmosphere.config.Settings;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;

/**
 * Vibracao de camera. Cada motor ligado soma uma contribuicao por tick (potencia x queda quadratica com a
 * distancia). No fim do tick o total vira um alvo 0..1 (saturacao suave) e a intensidade segue o alvo:
 * sobe rapido (ignicao -> aceleracao) e desce devagar (foguete se afastando / motor desligando).
 * O ruido e aplicado so ao calcular os angulos da camera (barato, sem alocacao).
 */
@EventBusSubscriber(modid = RocketAtmosphere.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
public final class CameraShake {
    private static final long T0 = System.nanoTime();

    private static double accum;       // soma deste tick
    private static double intensity;   // suavizada, 0..1

    private CameraShake() {}

    public static double intensity() { return intensity; }

    /** Chamado pelo ThrusterTracker uma vez por motor por tick. */
    static void addSource(double strength, double distance, double range) {
        double t = 1.0 - distance / range;
        if (t <= 0.0) return;
        accum += strength * t * t;
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        double target = accum > 0.0 ? 1.0 - Math.exp(-1.6 * accum) : 0.0;
        accum = 0.0;
        double k = target > intensity ? 0.25 : 0.06;     // ataque rapido, queda lenta (~1,5 s)
        intensity += (target - intensity) * k;
        if (intensity < 0.003) intensity = 0.0;
    }

    @SubscribeEvent
    public static void onCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        if (intensity <= 0.0) return;
        Settings cfg = ThrusterTracker.settings();
        if (!cfg.enabled || !cfg.shake || cfg.shakeStrength <= 0.0) return;

        double t = (System.nanoTime() - T0) * 1.0e-9;
        double a = Math.pow(intensity, 1.2) * cfg.shakeStrength;   // leve na ignicao, forte na aceleracao

        double pitch = Math.sin(t * 37.1) * 0.6 + Math.sin(t * 61.7 + 1.3) * 0.4;
        double yaw = Math.sin(t * 29.3 + 0.7) * 0.6 + Math.sin(t * 53.9 + 2.1) * 0.4;
        double roll = Math.sin(t * 23.7 + 1.9) * 0.7 + Math.sin(t * 47.3) * 0.3;

        event.setPitch(event.getPitch() + (float) (a * 0.9 * pitch));
        event.setYaw(event.getYaw() + (float) (a * 0.8 * yaw));
        event.setRoll(event.getRoll() + (float) (a * 1.4 * roll));
    }
}

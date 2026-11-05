package dev.rocketatmosphere.space;

import dev.rocketatmosphere.RocketAtmosphere;
import it.unimi.dsi.fastutil.ints.Int2ObjectSortedMap;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.neoforged.fml.ModList;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.Set;

/**
 * Integracao opcional com o Create: Cosmonautics (modId "rocketnautics", GPLv3) feita por REFLEXAO: este mod
 * nao liga contra o codigo dele. Metodos usados (todos publicos e estaticos/verificados no bytecode do jar):
 *  - api.orbit.DeepSpaceHelper.isDeepSpace(Level)
 *  - api.orbit.DeepSpaceHelper.getDataForDimension(Level) -> Optional&lt;PlanetDimensionData&gt;
 *  - content.orbit.universe.PlanetDimensionData.transitionHeight() / atmosphere()
 *  - SkyDataHandler.getHeightOffsetForLevel(ResourceKey)
 * Qualquer falha desliga a integracao (o detector cai para a estimativa sem Cosmonautics).
 */
public final class CosmonauticsBridge {
    private static final String MOD_ID = "rocketnautics";

    private static boolean initDone;
    private static boolean ok;
    private static Method isDeepSpaceLevel;
    private static Method getData;
    private static Method transitionHeight;
    private static Method atmosphere;
    private static Method heightOffset;

    private CosmonauticsBridge() {}

    public static boolean available() {
        init();
        return ok;
    }

    private static synchronized void init() {
        if (initDone) return;
        initDone = true;
        try {
            if (!ModList.get().isLoaded(MOD_ID)) return;
            ClassLoader cl = CosmonauticsBridge.class.getClassLoader();
            Class<?> helper = Class.forName("dev.devce.rocketnautics.api.orbit.DeepSpaceHelper", false, cl);
            Class<?> data = Class.forName("dev.devce.rocketnautics.content.orbit.universe.PlanetDimensionData", false, cl);
            Class<?> sky = Class.forName("dev.devce.rocketnautics.SkyDataHandler", false, cl);
            isDeepSpaceLevel = helper.getMethod("isDeepSpace", Level.class);
            getData = helper.getMethod("getDataForDimension", Level.class);
            transitionHeight = data.getMethod("transitionHeight");
            atmosphere = data.getMethod("atmosphere");
            heightOffset = sky.getMethod("getHeightOffsetForLevel", ResourceKey.class);
            ok = true;
            RocketAtmosphere.LOGGER.info("[RocketAtmosphere] integracao com Create: Cosmonautics ativa.");
        } catch (Throwable t) {
            ok = false;
            RocketAtmosphere.LOGGER.warn("[RocketAtmosphere] Cosmonautics encontrado, mas a API esperada nao existe; integracao desligada", t);
        }
    }

    private static void fail(Throwable t) {
        ok = false;
        RocketAtmosphere.LOGGER.warn("[RocketAtmosphere] falha ao consultar o Cosmonautics; integracao desligada", t);
    }

    public static boolean isDeepSpace(Level level) {
        if (!available()) return false;
        try {
            return (Boolean) isDeepSpaceLevel.invoke(null, level);
        } catch (Throwable t) {
            fail(t);
            return false;
        }
    }

    public static double heightOffset(Level level) {
        if (!available()) return 0.0;
        try {
            return (Double) heightOffset.invoke(null, level.dimension());
        } catch (Throwable t) {
            fail(t);
            return 0.0;
        }
    }

    /** Altura de transferencia (dimension_transfer_height) do planeta da dimensao, ou o fallback. */
    public static double transitionHeight(Level level, double fallback) {
        if (!available()) return fallback;
        try {
            Optional<?> data = (Optional<?>) getData.invoke(null, level);
            if (data.isEmpty()) return fallback;
            return ((Integer) transitionHeight.invoke(data.get())).doubleValue();
        } catch (Throwable t) {
            fail(t);
            return fallback;
        }
    }

    /**
     * Flags de atmosfera na altura Y, com a mesma regra do Cosmonautics (GlobalSpacePhysicsHandler.getFlags):
     * entrada de menor chave &gt;= Y em atmosphere(). Devolve nomes do enum AtmosphereFlags
     * (LOW_DENSITY, DROWNING), array vazio = ar normal, ou null se desconhecido.
     */
    @SuppressWarnings("unchecked")
    public static String[] atmosphereFlags(Level level, int y) {
        if (!available()) return null;
        try {
            Optional<?> data = (Optional<?>) getData.invoke(null, level);
            if (data.isEmpty()) return null;
            Int2ObjectSortedMap<?> map = (Int2ObjectSortedMap<?>) atmosphere.invoke(data.get());
            Int2ObjectSortedMap<?> tail = map.tailMap(y);
            if (tail.isEmpty()) return new String[0];
            Set<?> flags = (Set<?>) tail.get(tail.firstIntKey());
            String[] names = new String[flags.size()];
            int i = 0;
            for (Object f : flags) names[i++] = ((Enum<?>) f).name();
            return names;
        } catch (Throwable t) {
            fail(t);
            return null;
        }
    }
}

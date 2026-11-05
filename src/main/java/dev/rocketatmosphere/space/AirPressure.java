package dev.rocketatmosphere.space;

import dev.rocketatmosphere.RocketAtmosphere;
import dev.ryanhcode.sable.physics.config.dimension_physics.DimensionPhysicsData;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import org.joml.Vector3d;

/**
 * Pressao do ar via DimensionPhysicsData.getAirPressure (a mesma chamada estatica que o Create Propulsion usa
 * em calculateAtmosphericFactor). Devolve NaN se a API falhar; nao tenta de novo depois de uma falha.
 */
public final class AirPressure {
    private static boolean broken;

    private AirPressure() {}

    /** @return pressao limitada a 0..1, ou NaN se indisponivel. */
    public static double sample(Level level, double x, double y, double z) {
        if (broken) return Double.NaN;
        try {
            double p = DimensionPhysicsData.getAirPressure(level, new Vector3d(x, y, z));
            return Double.isNaN(p) ? Double.NaN : Mth.clamp(p, 0.0, 1.0);
        } catch (Throwable t) {
            broken = true;
            RocketAtmosphere.LOGGER.warn("[RocketAtmosphere] pressao do ar indisponivel; usando estimativa por altitude", t);
            return Double.NaN;
        }
    }
}

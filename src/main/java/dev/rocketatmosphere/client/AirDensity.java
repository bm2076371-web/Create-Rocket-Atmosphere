package dev.rocketatmosphere.client;

import dev.rocketatmosphere.RocketAtmosphere;
import dev.ryanhcode.sable.physics.config.dimension_physics.DimensionPhysicsData;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;

/**
 * Fator 0..1 de densidade do ar na posicao, a partir de DimensionPhysicsData.getAirPressure (a mesma
 * chamada estatica que o Create Propulsion usa em calculateAtmosphericFactor). Se a API mudar ou falhar,
 * cai para 1.0 (efeitos normais) e nao tenta de novo.
 */
final class AirDensity {
    private static boolean broken;

    private AirDensity() {}

    static double at(Level level, Vec3 pos) {
        if (broken) return 1.0;
        try {
            double p = DimensionPhysicsData.getAirPressure(level, new Vector3d(pos.x, pos.y, pos.z));
            return Double.isNaN(p) ? 1.0 : Mth.clamp(p, 0.0, 1.0);
        } catch (Throwable t) {
            broken = true;
            RocketAtmosphere.LOGGER.warn("[RocketAtmosphere] pressao do ar indisponivel; efeito de atmosfera desligado", t);
            return 1.0;
        }
    }
}

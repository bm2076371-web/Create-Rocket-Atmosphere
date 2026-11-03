package dev.propulsionteam.propulsionsimulated.particles.smoke;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;

/** STUB DE COMPILACAO (record real: scale, lifetime, r, g, b). Nao empacotado. */
public record ThrusterSmokeParticleData(float scale, int lifetime, float r, float g, float b) implements ParticleOptions {
    @Override
    public ParticleType<?> getType() { throw new UnsupportedOperationException("stub"); }
}

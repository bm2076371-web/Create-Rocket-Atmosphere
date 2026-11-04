package dev.rocketatmosphere.config;

/**
 * Copia imutavel da config + perfil de qualidade. Lida 1x por segundo (nao a cada particula/frame).
 */
public final class Settings {
    public static final Settings DISABLED = new Settings(false, QualityPreset.MEDIUM, 0, 1.0,
            false, false, false, false, false, false, 0.0);

    public final boolean enabled;
    public final boolean flames;
    public final boolean trailSmoke;
    public final boolean groundSmoke;
    public final boolean airDensity;
    public final boolean shake;
    public final boolean electric;
    public final QualityPreset preset;
    public final double maxDistance;
    public final double particleScale;
    public final double shakeStrength;

    // derivados do perfil
    public final double density;
    public final double lifetimeScale;
    public final double groundRange;
    public final double shakeRange;
    public final int emitInterval;
    public final int groundInterval;
    public final int budgetPerTick;

    private Settings(boolean enabled, QualityPreset preset, int distanceOverride, double particleScale,
                     boolean flames, boolean trailSmoke, boolean groundSmoke, boolean airDensity,
                     boolean electric, boolean shake, double shakeStrength) {
        this.enabled = enabled;
        this.preset = preset;
        this.maxDistance = distanceOverride > 0 ? distanceOverride : preset.maxDistance;
        this.particleScale = particleScale;
        this.flames = flames;
        this.trailSmoke = trailSmoke;
        this.groundSmoke = groundSmoke;
        this.airDensity = airDensity;
        this.electric = electric;
        this.shake = shake;
        this.shakeStrength = shakeStrength;
        this.density = preset.density;
        this.lifetimeScale = preset.lifetimeScale;
        this.groundRange = preset.groundRange;
        this.shakeRange = preset.shakeRange;
        this.emitInterval = preset.emitInterval;
        this.groundInterval = preset.groundInterval;
        this.budgetPerTick = preset.budgetPerTick;
    }

    public static Settings read() {
        try {
            return new Settings(
                    RAConfig.ENABLED.get(),
                    RAConfig.QUALITY.get(),
                    RAConfig.MAX_DISTANCE.get(),
                    RAConfig.PARTICLE_SCALE.get(),
                    RAConfig.FLAMES.get(),
                    RAConfig.TRAIL_SMOKE.get(),
                    RAConfig.GROUND_SMOKE.get(),
                    RAConfig.AIR_DENSITY.get(),
                    RAConfig.ELECTRIC.get(),
                    RAConfig.SHAKE_ENABLED.get(),
                    RAConfig.SHAKE_STRENGTH.get());
        } catch (IllegalStateException configNotLoadedYet) {
            return DISABLED;
        }
    }
}

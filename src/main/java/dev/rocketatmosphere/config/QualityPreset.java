package dev.rocketatmosphere.config;

/** Perfis de qualidade. Todos os numeros sao pontos de ajuste, nao valores "oficiais". */
public enum QualityPreset {
    //  distMax densidade ticksEmissao ticksChao alcanceChao vidaParticula orcamento/tick alcanceShake
    LOW(48, 0.35, 3, 8, 10.0, 0.60, 12, 32.0),
    MEDIUM(80, 0.70, 2, 5, 14.0, 0.85, 28, 48.0),
    HIGH(128, 1.00, 1, 4, 20.0, 1.00, 64, 64.0);

    public final int maxDistance;
    public final double density;
    public final int emitInterval;
    public final int groundInterval;
    public final double groundRange;
    public final double lifetimeScale;
    public final int budgetPerTick;
    public final double shakeRange;

    QualityPreset(int maxDistance, double density, int emitInterval, int groundInterval,
                  double groundRange, double lifetimeScale, int budgetPerTick, double shakeRange) {
        this.maxDistance = maxDistance;
        this.density = density;
        this.emitInterval = emitInterval;
        this.groundInterval = groundInterval;
        this.groundRange = groundRange;
        this.lifetimeScale = lifetimeScale;
        this.budgetPerTick = budgetPerTick;
        this.shakeRange = shakeRange;
    }
}

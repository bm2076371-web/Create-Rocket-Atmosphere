package dev.rocketatmosphere.space;

import dev.rocketatmosphere.config.Settings;

/** Resultado imutavel de uma amostragem do detector de espaco. */
public final class SpaceState {
    public static final SpaceState NEUTRAL = new SpaceState(SpaceZone.ATMOSPHERE, 0.0, false, 0.0, false, false,
            false, false, false, 1.0, Double.NaN, 20000.0);

    public final SpaceZone zone;
    /** Y + deslocamento de altura da dimensao (o mesmo valor que o Cosmonautics usa para o visual do espaco). */
    public final double altitude;
    /** Detector simples: altitude >= altitude fixa configurada. */
    public final boolean aboveFixedAltitude;
    /** 0..1: 0 = ceu normal (Y 1000), 1 = ceu totalmente preto (Y 3000), como no Cosmonautics. */
    public final double visualFraction;
    public final boolean deepSpaceDimension;
    public final boolean cosmonauticsActive;
    public final boolean flagsKnown;
    public final boolean lowDensity;
    public final boolean drowning;
    /** 0..1 (1 = ar normal, 0 = vacuo). */
    public final double density;
    /** Pressao bruta do Sable (NaN se indisponivel). */
    public final double pressure;
    public final double transferHeight;

    public SpaceState(SpaceZone zone, double altitude, boolean aboveFixedAltitude, double visualFraction,
                      boolean deepSpaceDimension, boolean cosmonauticsActive, boolean flagsKnown,
                      boolean lowDensity, boolean drowning, double density, double pressure, double transferHeight) {
        this.zone = zone;
        this.altitude = altitude;
        this.aboveFixedAltitude = aboveFixedAltitude;
        this.visualFraction = visualFraction;
        this.deepSpaceDimension = deepSpaceDimension;
        this.cosmonauticsActive = cosmonauticsActive;
        this.flagsKnown = flagsKnown;
        this.lowDensity = lowDensity;
        this.drowning = drowning;
        this.density = density;
        this.pressure = pressure;
        this.transferHeight = transferHeight;
    }

    public boolean airless() {
        return density <= 0.02;
    }

    public String describe(Settings cfg) {
        return "[Rocket Atmosphere] zona=" + zone
                + "\n altitude=" + String.format("%.0f", altitude)
                + " | detector simples (>= " + String.format("%.0f", cfg.fixedAltitude) + "): " + (aboveFixedAltitude ? "NO ESPACO" : "abaixo")
                + "\n densidade=" + String.format("%.2f", density)
                + " | pressao Sable=" + (Double.isNaN(pressure) ? "indisponivel" : String.format("%.2f", pressure))
                + " | sem ar=" + airless()
                + "\n Cosmonautics=" + (cosmonauticsActive ? "ativo" : "ausente/desligado")
                + " | dimensao espacial=" + deepSpaceDimension
                + " | flags de atmosfera=" + (flagsKnown ? ((lowDensity ? "LOW_DENSITY " : "") + (drowning ? "DROWNING" : "")).trim() : "desconhecidas")
                + "\n transicao p/ espaco profundo em Y=" + String.format("%.0f", transferHeight)
                + " | fracao visual do espaco=" + String.format("%.2f", visualFraction);
    }
}

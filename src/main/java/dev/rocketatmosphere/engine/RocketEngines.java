package dev.rocketatmosphere.engine;

import java.util.List;

import static dev.rocketatmosphere.engine.RocketFluids.*;

/**
 * Motores disponiveis. Valores relativos ao Thruster comum (balanceamento, ajuste livre).
 * Razoes volumetricas LOX/combustivel: CH4 1,33 | RP-1 ~1,6-1,9 | LH2 ~0,37.
 */
public final class RocketEngines {
    private static final String M = "fluid.rocketatmosphere.methane";
    private static final String R = "fluid.rocketatmosphere.rp1";
    private static final String H = "fluid.rocketatmosphere.liquid_hydrogen";
    private static final String O = "fluid.rocketatmosphere.liquid_oxygen";

    // Starship / Super Heavy
    public static final RocketEngineSpec RAPTOR_3 = new RocketEngineSpec("raptor_3", METHANE_TAG, LOX_TAG,
            4.0, 4.0f, 1.33, false, 1.0, 1.5, 6000, 1.0, M, O);
    public static final RocketEngineSpec RAPTOR_VACUUM = new RocketEngineSpec("raptor_vacuum", METHANE_TAG, LOX_TAG,
            3.6, 3.2f, 1.33, true, 0.35, 1.5, 6000, 0.9, M, O);
    // Falcon 9
    public static final RocketEngineSpec MERLIN_1D = new RocketEngineSpec("merlin_1d", RP1_TAG, LOX_TAG,
            1.6, 1.8f, 1.68, false, 1.0, 1.5, 4000, 1.5, R, O);
    // Space Shuttle / SLS
    public static final RocketEngineSpec RS_25 = new RocketEngineSpec("rs_25", LH2_TAG, LOX_TAG,
            3.2, 6.4f, 0.375, false, 1.0, 1.5, 12000, 0.35, H, O);
    // Saturn V
    public static final RocketEngineSpec F_1 = new RocketEngineSpec("f_1", RP1_TAG, LOX_TAG,
            8.0, 9.6f, 1.61, false, 1.0, 1.5, 12000, 1.7, R, O);
    // Vulcan / New Glenn
    public static final RocketEngineSpec BE_4 = new RocketEngineSpec("be_4", METHANE_TAG, LOX_TAG,
            3.6, 3.6f, 1.33, false, 1.0, 1.5, 6000, 1.0, M, O);
    // Atlas V / Soyuz-like
    public static final RocketEngineSpec RD_180 = new RocketEngineSpec("rd_180", RP1_TAG, LOX_TAG,
            5.0, 5.8f, 1.93, false, 1.0, 1.5, 8000, 1.5, R, O);
    // Electron
    public static final RocketEngineSpec RUTHERFORD = new RocketEngineSpec("rutherford", RP1_TAG, LOX_TAG,
            0.5, 0.6f, 1.63, false, 1.0, 1.0, 2000, 1.2, R, O);
    // Estagio superior (Centaur): so rende no vacuo
    public static final RocketEngineSpec RL10 = new RocketEngineSpec("rl10", LH2_TAG, LOX_TAG,
            0.8, 1.6f, 0.366, true, 0.05, 1.5, 8000, 0.3, H, O);

    public static final List<RocketEngineSpec> ALL = List.of(
            RAPTOR_3, RAPTOR_VACUUM, MERLIN_1D, RS_25, F_1, BE_4, RD_180, RUTHERFORD, RL10);

    private RocketEngines() {}
}

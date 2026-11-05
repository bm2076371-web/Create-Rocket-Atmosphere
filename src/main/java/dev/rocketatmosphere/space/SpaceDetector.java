package dev.rocketatmosphere.space;

import dev.rocketatmosphere.config.Settings;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;

/**
 * Detectores de espaco.
 *
 * 1) SIMPLES: {@link #isAboveFixedAltitude}: altitude fixa configuravel (padrao 3000).
 * 2) AMBIENTAL: {@link #sample}: dimensao espacial, flags de atmosfera, densidade do ar e altitude.
 *
 * Valores do Create: Cosmonautics (verificados no bytecode do jar 26.08.307):
 *  - Y 1000: neblina/ceu comecam a mudar, sol e lua de vanilla somem, estrelas aparecem
 *            (SkyHandler.onRenderFog, LevelRendererMixin: condicao altura &gt; 1000)
 *  - Y 2000: neblina totalmente removida (clamp((h-1000)/1000))
 *  - Y 3000: ceu totalmente preto (clamp((h-1000)/2000))
 *  - Y 5000: acima disso o Overworld tem LOW_DENSITY + DROWNING (atmosphere_composition do planeta)
 *  - Y 20000: transferencia do foguete para a dimensao de espaco profundo
 *            (SpaceTransitionHandler.OVERWORLD_SPACE_Y; dimension_transfer_height do planeta)
 */
public final class SpaceDetector {
    public static final double VISUAL_START = 1000.0;
    public static final double VISUAL_FULL = 3000.0;
    public static final double TRANSFER_DEFAULT = 20000.0;

    private SpaceDetector() {}

    /** Detector simples por altitude fixa. */
    public static boolean isAboveFixedAltitude(double altitude, Settings cfg) {
        return altitude >= cfg.fixedAltitude;
    }

    /** Detector ambiental. Seguro para chamar a cada ~10 ticks por motor. */
    public static SpaceState sample(Level level, double x, double y, double z, Settings cfg) {
        boolean cosm = cfg.cosmonautics && CosmonauticsBridge.available();
        boolean deep = cosm && CosmonauticsBridge.isDeepSpace(level);
        double offset = cosm ? CosmonauticsBridge.heightOffset(level) : 0.0;
        double h = y + offset;
        double transfer = cosm ? CosmonauticsBridge.transitionHeight(level, TRANSFER_DEFAULT) : TRANSFER_DEFAULT;

        String[] flags = (cosm && !deep) ? CosmonauticsBridge.atmosphereFlags(level, (int) y) : null;
        boolean flagsKnown = deep || flags != null;
        boolean low = deep;
        boolean drowning = deep;
        if (flags != null) {
            for (String f : flags) {
                if ("LOW_DENSITY".equals(f)) low = true;
                else if ("DROWNING".equals(f)) drowning = true;
            }
        }

        double pressure = deep ? 0.0 : AirPressure.sample(level, x, y, z);
        double visual = Mth.clamp((h - VISUAL_START) / (VISUAL_FULL - VISUAL_START), 0.0, 1.0);

        double density;
        if (deep) {
            density = 0.0;
        } else {
            density = Double.isNaN(pressure) ? 1.0 - visual : pressure;   // sem Sable: estima pelo mesmo degrade visual
            if (low) density = Math.min(density, 0.35);
        }

        boolean aboveFixed = h >= cfg.fixedAltitude;
        SpaceZone zone;
        if (deep || aboveFixed || density <= 0.02) zone = SpaceZone.SPACE;
        else if (h >= VISUAL_START || low || density < 0.5) zone = SpaceZone.THIN_ATMOSPHERE;
        else zone = SpaceZone.ATMOSPHERE;

        return new SpaceState(zone, h, aboveFixed, visual, deep, cosm, flagsKnown, low, drowning,
                density, pressure, transfer);
    }
}

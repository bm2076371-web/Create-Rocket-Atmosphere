package dev.rocketatmosphere.client;

import dev.propulsionteam.propulsionsimulated.particles.smoke.ThrusterSmokeParticleData;
import dev.rocketatmosphere.RocketAtmosphere;
import dev.rocketatmosphere.config.Settings;
import dev.rocketatmosphere.space.SpaceDetector;
import dev.rocketatmosphere.space.SpaceState;
import dev.rocketatmosphere.space.SpaceZone;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

/**
 * Emissao de particulas. Fumaca usa a particula ja registrada pelo Create Propulsion
 * (ThrusterSmokeParticleData: escala, vida, cor); chamas usam particulas de vanilla.
 * Quantidades passam por: perfil de qualidade, LOD por distancia, densidade do ar (detector de espaco) e
 * um orcamento global de particulas por tick.
 */
final class ThrusterEffects {
    /** Acima disso as particulas sao forcadas (vanilla descarta particulas normais alem de ~32 blocos). */
    private static final double FORCE_DISTANCE = 28.0;
    private static final int SPACE_REFRESH_TICKS = 10;

    private static long budgetTick = Long.MIN_VALUE;
    private static int budget;

    private ThrusterEffects() {}

    static void emit(Level level, Settings cfg, ThrusterState st, double throttle, double dist, long now, boolean electric, double smoke) {
        RandomSource rnd = level.getRandom();
        double lod = 1.0 - 0.65 * Math.min(1.0, dist / cfg.maxDistance);   // longe = menos particulas
        boolean force = dist > FORCE_DISTANCE;

        // Detector ambiental (barato: refeito a cada 10 ticks por motor)
        SpaceState sp = SpaceState.NEUTRAL;
        if (cfg.airDensity || cfg.spaceEffects) {
            if (st.space == null || now >= st.nextSpace) {
                st.space = SpaceDetector.sample(level, st.nozzle.x, st.nozzle.y, st.nozzle.z, cfg);
                st.nextSpace = now + SPACE_REFRESH_TICKS;
                if (st.space.zone != st.lastZone) {
                    RocketAtmosphere.LOGGER.debug("[RocketAtmosphere] motor em {}: zona {} -> {}", st.nozzle, st.lastZone, st.space.zone);
                    st.lastZone = st.space.zone;
                }
            }
            sp = st.space;
        }
        double air = cfg.airDensity ? sp.density : 1.0;
        boolean vacuum = cfg.spaceEffects && sp.zone == SpaceZone.SPACE;

        if (cfg.flames) {
            flames(level, rnd, cfg, st, throttle, cfg.density * lod, now, electric, vacuum);
        }
        if (air > 0.08) {                                                  // sem fumaca no vacuo
            double k = cfg.density * lod * air * smoke;                     // RP-1 solta mais fumaca; hidrogenio quase nenhuma
            float shade = smoke > 1.2 ? 0.72f : (smoke < 0.5 ? 0.97f : 0.9f);   // fuligem escura x vapor branco
            if (cfg.trailSmoke) trail(level, rnd, cfg, st, throttle, k * (electric ? 0.45 : 1.0), force, now, electric, shade);
            if (cfg.groundSmoke) ground(level, rnd, cfg, st, throttle, k * (electric ? 0.5 : 1.0), force, now, electric, shade - 0.02f);
        }
        st.lastEmit = st.nozzle;
    }

    // ---------------------------------------------------------------- chamas / exaustao
    private static void flames(Level level, RandomSource rnd, Settings cfg, ThrusterState st,
                               double throttle, double k, long now, boolean electric, boolean vacuum) {
        // eletrico: poucas faiscas brilhantes (END_ROD); combustao: chamas. No vacuo o jato se abre e vai mais longe.
        int n = take(now, cfg, stochastic((electric ? 0.5 + 1.5 * throttle : 1.0 + 3.0 * throttle) * k * (vacuum ? 1.3 : 1.0), rnd));
        Vec3 d = st.dir;
        double spread = vacuum ? 0.30 : 0.12;
        for (int i = 0; i < n; i++) {
            double along = rnd.nextDouble() * (vacuum ? 0.8 : 0.45);
            double px = st.nozzle.x + d.x * along + jitter(rnd, spread);
            double py = st.nozzle.y + d.y * along + jitter(rnd, spread);
            double pz = st.nozzle.z + d.z * along + jitter(rnd, spread);
            double sp = (electric ? 0.05 + 0.10 * throttle : 0.07 + 0.15 * throttle) * (vacuum ? 1.6 : 1.0);
            // herda so um pouco da velocidade do foguete: as faiscas ficam para tras
            double vx = d.x * sp + st.vx * 0.15 + jitter(rnd, vacuum ? 0.05 : 0.02);
            double vy = d.y * sp + st.vy * 0.15 + jitter(rnd, vacuum ? 0.05 : 0.02);
            double vz = d.z * sp + st.vz * 0.15 + jitter(rnd, vacuum ? 0.05 : 0.02);
            ParticleOptions opt = electric ? ParticleTypes.END_ROD
                    : (rnd.nextFloat() < 0.65f ? ParticleTypes.FLAME : ParticleTypes.SMALL_FLAME);
            level.addParticle(opt, px, py, pz, vx, vy, vz);
        }
    }

    // ---------------------------------------------------------------- fumaca nos motores + rastro
    private static void trail(Level level, RandomSource rnd, Settings cfg, ThrusterState st,
                              double throttle, double k, boolean force, long now, boolean electric, float shade) {
        double travelled = st.lastEmit == null ? 0.0 : st.lastEmit.distanceTo(st.nozzle);
        if (travelled > 16.0) travelled = 0.0;
        double base = Math.max(1.0, travelled / 1.1);                      // ~1 puff por 1.1 bloco percorrido
        int n = take(now, cfg, stochastic(base * (0.6 + 0.8 * throttle) * k, rnd));
        Vec3 d = st.dir;
        for (int i = 0; i < n; i++) {
            double f = (i + rnd.nextDouble()) / n;                          // posicao ao longo do caminho
            double ox = st.nozzle.x, oy = st.nozzle.y, oz = st.nozzle.z;
            if (st.lastEmit != null && travelled > 0.0) {
                ox = Mth.lerp(f, st.lastEmit.x, st.nozzle.x);
                oy = Mth.lerp(f, st.lastEmit.y, st.nozzle.y);
                oz = Mth.lerp(f, st.lastEmit.z, st.nozzle.z);
            }
            double out = 0.5 + rnd.nextDouble() * 0.8;
            double px = ox + d.x * out + jitter(rnd, 0.3);
            double py = oy + d.y * out + jitter(rnd, 0.3);
            double pz = oz + d.z * out + jitter(rnd, 0.3);
            double sp = 0.02 + 0.05 * throttle;
            double vx = d.x * sp + st.vx * 0.04 + jitter(rnd, 0.015);
            double vy = d.y * sp + st.vy * 0.04 + jitter(rnd, 0.015);
            double vz = d.z * sp + st.vz * 0.04 + jitter(rnd, 0.015);
            float scale = (float) ((1.6 + 2.0 * throttle) * cfg.particleScale * (electric ? 0.7 : 1.0) * (0.8 + 0.4 * rnd.nextDouble()));
            int life = (int) ((45 + 45 * throttle) * cfg.lifetimeScale * (0.8 + 0.4 * rnd.nextDouble()));
            puff(level, force, px, py, pz, vx, vy, vz, scale, life, shade, electric);
        }
    }

    // ---------------------------------------------------------------- nuvem de fumaca no chao
    private static void ground(Level level, RandomSource rnd, Settings cfg, ThrusterState st,
                               double throttle, double k, boolean force, long now, boolean electric, float shade) {
        if (now >= st.nextGround) {                                         // raycast barato, espacado
            st.nextGround = now + cfg.groundInterval;
            Vec3 from = st.nozzle.add(st.dir.scale(0.6));
            Vec3 to = st.nozzle.add(st.dir.scale(cfg.groundRange));
            BlockHitResult hit = level.clip(new ClipContext(from, to,
                    ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, CollisionContext.empty()));
            if (hit.getType() == HitResult.Type.MISS) {
                st.groundDist = -1;
            } else {
                Direction face = hit.getDirection();
                st.groundDist = from.distanceTo(hit.getLocation()) + 0.6;
                st.groundPoint = hit.getLocation();
                st.groundNormal = new Vec3(face.getStepX(), face.getStepY(), face.getStepZ());
            }
        }
        if (st.groundDist < 0) return;                                      // decolou: para de gerar (as nuvens antigas se dissipam sozinhas)

        double prox = 1.0 - Math.min(1.0, st.groundDist / cfg.groundRange);
        double intensity = Math.pow(prox, 0.7) * throttle;
        int n = take(now, cfg, stochastic((2.0 + 6.0 * intensity) * k, rnd));
        if (n <= 0) return;

        Vec3 nrm = st.groundNormal;
        Vec3 helper = Math.abs(nrm.y) < 0.9 ? new Vec3(0, 1, 0) : new Vec3(1, 0, 0);
        Vec3 t1 = nrm.cross(helper).normalize();
        Vec3 t2 = nrm.cross(t1);
        for (int i = 0; i < n; i++) {
            double ang = rnd.nextDouble() * (Math.PI * 2.0);
            double rx = t1.x * Math.cos(ang) + t2.x * Math.sin(ang);
            double ry = t1.y * Math.cos(ang) + t2.y * Math.sin(ang);
            double rz = t1.z * Math.cos(ang) + t2.z * Math.sin(ang);
            double r0 = 0.4 + rnd.nextDouble() * 0.8;
            double px = st.groundPoint.x + nrm.x * 0.25 + rx * r0;
            double py = st.groundPoint.y + nrm.y * 0.25 + ry * r0;
            double pz = st.groundPoint.z + nrm.z * 0.25 + rz * r0;
            double sp = 0.12 + 0.25 * intensity;                            // espalha rente a superficie
            double vx = rx * sp + nrm.x * 0.02;
            double vy = ry * sp + nrm.y * 0.02;
            double vz = rz * sp + nrm.z * 0.02;
            float scale = (float) ((2.6 + 3.0 * intensity) * cfg.particleScale * (electric ? 0.8 : 1.0) * (0.8 + 0.4 * rnd.nextDouble()));
            int life = (int) ((60 + 60 * intensity) * cfg.lifetimeScale * (0.8 + 0.4 * rnd.nextDouble()));
            puff(level, force, px, py, pz, vx, vy, vz, scale, life, shade, electric);
        }
    }

    // ---------------------------------------------------------------- utilidades
    private static void puff(Level level, boolean force, double x, double y, double z,
                             double vx, double vy, double vz, float scale, int life, float shade, boolean electric) {
        // combustao: cinza levemente quente; eletrico: branco azulado
        float r = electric ? shade * 0.93f : shade;
        float g = electric ? shade * 0.97f : shade;
        float b = electric ? shade : shade * 0.97f;
        level.addParticle(new ThrusterSmokeParticleData(scale, Math.max(10, life), r, g, b),
                force, x, y, z, vx, vy, vz);
    }

    /** Orcamento global por tick: nunca passa de cfg.budgetPerTick particulas somando todos os motores. */
    private static int take(long now, Settings cfg, int want) {
        if (now != budgetTick) {
            budgetTick = now;
            budget = cfg.budgetPerTick;
        }
        int granted = Math.min(want, budget);
        budget -= granted;
        return granted;
    }

    /** Arredondamento estocastico: 0.35 vira 1 particula em 35% das vezes (evita zerar em LOW). */
    private static int stochastic(double x, RandomSource rnd) {
        int n = (int) x;
        return n + (rnd.nextDouble() < x - n ? 1 : 0);
    }

    private static double jitter(RandomSource rnd, double amp) {
        return (rnd.nextDouble() - 0.5) * 2.0 * amp;
    }
}

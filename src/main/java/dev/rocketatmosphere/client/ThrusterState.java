package dev.rocketatmosphere.client;

import dev.rocketatmosphere.space.SpaceState;
import dev.rocketatmosphere.space.SpaceZone;
import net.minecraft.world.phys.Vec3;

/** Estado leve por motor (guardado em WeakHashMap; some quando o BlockEntity e descartado). */
final class ThrusterState {
    boolean valid;
    Vec3 nozzle = Vec3.ZERO;          // saida do bocal, coordenadas de MUNDO
    Vec3 dir = new Vec3(0, -1, 0);    // direcao do escape, MUNDO, normalizada
    double vx, vy, vz;                // velocidade estimada do bocal (blocos/tick), suavizada

    long lastRayTick = Long.MIN_VALUE;
    long nextRay = Long.MIN_VALUE;
    long nextEmit = Long.MIN_VALUE;
    long nextGround = Long.MIN_VALUE;

    SpaceState space;                 // ultima amostra do detector de espaco
    long nextSpace = Long.MIN_VALUE;
    SpaceZone lastZone;

    Vec3 lastEmit;                    // posicao do bocal na ultima emissao (para espalhar o rastro)

    double groundDist = -1;           // distancia ate o chao no eixo do escape (-1 = sem chao no alcance)
    Vec3 groundPoint = Vec3.ZERO;
    Vec3 groundNormal = new Vec3(0, 1, 0);

    void updateRay(Vec3 newNozzle, Vec3 newDir, long now) {
        long dt = now - lastRayTick;
        if (valid && dt > 0 && dt <= 6) {
            double inv = 1.0 / dt;
            vx += ((newNozzle.x - nozzle.x) * inv - vx) * 0.5;
            vy += ((newNozzle.y - nozzle.y) * inv - vy) * 0.5;
            vz += ((newNozzle.z - nozzle.z) * inv - vz) * 0.5;
            double sp = speed();
            if (sp > 6.0) {
                double s = 6.0 / sp;
                vx *= s; vy *= s; vz *= s;
            }
        } else {
            vx = vy = vz = 0;
            lastEmit = null;
        }
        nozzle = newNozzle;
        dir = newDir;
        lastRayTick = now;
        valid = true;
    }

    double speed() {
        return Math.sqrt(vx * vx + vy * vy + vz * vz);
    }
}

package dev.rocketatmosphere.engine;

import dev.propulsionteam.propulsionsimulated.content.thruster.FluidThrusterProperties;
import dev.propulsionteam.propulsionsimulated.content.thruster.ThrusterParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;

import java.util.List;

/**
 * Descricao de um motor de foguete bipropelente. Os valores de empuxo/consumo sao RELATIVOS ao Thruster comum do
 * Create Propulsion (pontos de balanceamento, nao dados reais); a razao oxidante/combustivel e volumetrica
 * (O/F em massa x densidade do combustivel / densidade do LOX).
 *
 * @param id               id do bloco (rocketatmosphere:id)
 * @param fuelTag          tag do combustivel
 * @param oxidizerTag      tag do oxidante
 * @param thrustMultiplier empuxo relativo ao Thruster comum
 * @param consumptionMult  consumo de combustivel relativo ao Thruster comum
 * @param oxidizerPerFuel  mB de oxidante por mB de combustivel
 * @param vacuumOptimized  se true, o empuxo cai na atmosfera (bocal de vacuo)
 * @param seaLevelFactor   fracao do empuxo ao nivel do mar quando vacuumOptimized (1.0 = sem perda)
 * @param nozzleOffset     distancia do centro do bloco ate a boca do bocal, em blocos
 * @param tankMb           capacidade de cada tanque em mB
 * @param smokeFactor      fumaca relativa (RP-1 solta mais, hidrogenio quase nada)
 */
public record RocketEngineSpec(String id, TagKey<Fluid> fuelTag, TagKey<Fluid> oxidizerTag,
                               double thrustMultiplier, float consumptionMult, double oxidizerPerFuel,
                               boolean vacuumOptimized, double seaLevelFactor, double nozzleOffset,
                               int tankMb, double smokeFactor, String fuelNameKey, String oxidizerNameKey) {

    public FluidThrusterProperties properties() {
        return new FluidThrusterProperties(1.0f, consumptionMult, ThrusterParticleType.PLUME,
                List.<ResourceLocation>of(), null, false);
    }
}

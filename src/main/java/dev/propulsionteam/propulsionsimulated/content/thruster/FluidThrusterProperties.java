package dev.propulsionteam.propulsionsimulated.content.thruster;

import net.minecraft.resources.ResourceLocation;

import java.util.List;

/** STUB DE COMPILACAO (record real, javap). Nao empacotado. */
public record FluidThrusterProperties(float thrustMultiplier, float consumptionMultiplier,
                                      ThrusterParticleType particleType, List<ResourceLocation> overrideTextures,
                                      Integer overrideColor, boolean useFluidColor) {
}

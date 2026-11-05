package dev.rocketatmosphere.raptor;

import dev.rocketatmosphere.RocketAtmosphere;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;

/** Tags de fluido aceitas pelo Raptor 3 (datapack: data/rocketatmosphere/tags/fluid/). */
public final class RaptorFluids {
    public static final TagKey<Fluid> METHANE_TAG = TagKey.create(Registries.FLUID,
            ResourceLocation.fromNamespaceAndPath(RocketAtmosphere.MOD_ID, "methane"));
    public static final TagKey<Fluid> LOX_TAG = TagKey.create(Registries.FLUID,
            ResourceLocation.fromNamespaceAndPath(RocketAtmosphere.MOD_ID, "lox"));

    private RaptorFluids() {}

    public static boolean isMethaneStack(FluidStack stack) {
        return !stack.isEmpty() && stack.is(METHANE_TAG);
    }

    public static boolean isLoxStack(FluidStack stack) {
        return !stack.isEmpty() && stack.is(LOX_TAG);
    }

    public static boolean isMethaneFluid(Fluid fluid) {
        return fluid.is(METHANE_TAG);
    }
}

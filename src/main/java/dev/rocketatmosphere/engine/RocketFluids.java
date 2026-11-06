package dev.rocketatmosphere.engine;

import dev.rocketatmosphere.RocketAtmosphere;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;

/** Tags de fluido (datapack: data/rocketatmosphere/tags/fluid/). */
public final class RocketFluids {
    public static final TagKey<Fluid> METHANE_TAG = tag("methane");
    public static final TagKey<Fluid> LOX_TAG = tag("lox");
    public static final TagKey<Fluid> RP1_TAG = tag("rp1");
    public static final TagKey<Fluid> LH2_TAG = tag("lh2");

    private RocketFluids() {}

    private static TagKey<Fluid> tag(String name) {
        return TagKey.create(Registries.FLUID, ResourceLocation.fromNamespaceAndPath(RocketAtmosphere.MOD_ID, name));
    }

    public static boolean matches(FluidStack stack, TagKey<Fluid> tag) {
        return !stack.isEmpty() && stack.is(tag);
    }
}

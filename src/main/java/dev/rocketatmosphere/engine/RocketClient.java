package dev.rocketatmosphere.engine;

import dev.rocketatmosphere.RocketAtmosphere;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

/** Texturas dos fluidos (cliente): block/<nome>_still e block/<nome>_flow. */
@EventBusSubscriber(modid = RocketAtmosphere.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class RocketClient {
    private RocketClient() {}

    @SubscribeEvent
    public static void onRegisterClientExtensions(RegisterClientExtensionsEvent event) {
        for (RocketRegistries.RocketFluid fluid : RocketRegistries.FLUID_LIST) {
            event.registerFluidType(extensions(fluid.name()), fluid.type().get());
        }
    }

    private static IClientFluidTypeExtensions extensions(String name) {
        ResourceLocation still = ResourceLocation.fromNamespaceAndPath(RocketAtmosphere.MOD_ID, "block/" + name + "_still");
        ResourceLocation flow = ResourceLocation.fromNamespaceAndPath(RocketAtmosphere.MOD_ID, "block/" + name + "_flow");
        return new IClientFluidTypeExtensions() {
            @Override
            public ResourceLocation getStillTexture() { return still; }

            @Override
            public ResourceLocation getFlowingTexture() { return flow; }
        };
    }
}

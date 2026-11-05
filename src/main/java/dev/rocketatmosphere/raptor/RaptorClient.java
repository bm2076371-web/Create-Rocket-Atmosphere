package dev.rocketatmosphere.raptor;

import dev.rocketatmosphere.RocketAtmosphere;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

/** Texturas dos fluidos (cliente). */
@EventBusSubscriber(modid = RocketAtmosphere.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class RaptorClient {
    private RaptorClient() {}

    @SubscribeEvent
    public static void onRegisterClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerFluidType(extensions("methane"), RaptorRegistries.METHANE_TYPE.get());
        event.registerFluidType(extensions("liquid_oxygen"), RaptorRegistries.LOX_TYPE.get());
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

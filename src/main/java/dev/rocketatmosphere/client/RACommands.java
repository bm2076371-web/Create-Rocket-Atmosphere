package dev.rocketatmosphere.client;

import com.mojang.brigadier.context.CommandContext;
import dev.rocketatmosphere.RocketAtmosphere;
import dev.rocketatmosphere.config.QualityPreset;
import dev.rocketatmosphere.config.RAConfig;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;

/**
 * Comandos de cliente (uteis no celular, sem editar arquivo):
 *   /rocketatmosphere quality low|medium|high
 *   /rocketatmosphere status
 */
@EventBusSubscriber(modid = RocketAtmosphere.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
public final class RACommands {
    private RACommands() {}

    @SubscribeEvent
    public static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("rocketatmosphere")
                .then(Commands.literal("quality")
                        .then(Commands.literal("low").executes(c -> setQuality(c, QualityPreset.LOW)))
                        .then(Commands.literal("medium").executes(c -> setQuality(c, QualityPreset.MEDIUM)))
                        .then(Commands.literal("high").executes(c -> setQuality(c, QualityPreset.HIGH))))
                .then(Commands.literal("status").executes(RACommands::status)));
    }

    private static int setQuality(CommandContext<CommandSourceStack> ctx, QualityPreset preset) {
        RAConfig.QUALITY.set(preset);
        RAConfig.QUALITY.save();
        ThrusterTracker.refreshSettings();
        ctx.getSource().sendSuccess(() -> Component.literal("[Rocket Atmosphere] qualidade: " + preset.name()), false);
        return 1;
    }

    private static int status(CommandContext<CommandSourceStack> ctx) {
        String msg = "[Rocket Atmosphere] qualidade=" + ThrusterTracker.settings().preset.name()
                + " | gancho ativo=" + (ThrusterTracker.hookSeen() ? "sim" : "ainda nao (nenhum motor visto)")
                + " | motores ativos rastreados=" + ThrusterTracker.trackedCount()
                + " | vibracao=" + String.format("%.2f", CameraShake.intensity())
                + (ThrusterTracker.failed() ? " | DESATIVADO por erros (veja latest.log)" : "");
        ctx.getSource().sendSuccess(() -> Component.literal(msg), false);
        return 1;
    }
}

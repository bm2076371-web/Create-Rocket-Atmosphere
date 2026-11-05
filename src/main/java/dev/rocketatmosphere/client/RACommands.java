package dev.rocketatmosphere.client;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.context.CommandContext;
import dev.rocketatmosphere.RocketAtmosphere;
import dev.rocketatmosphere.config.QualityPreset;
import dev.rocketatmosphere.config.RAConfig;
import dev.rocketatmosphere.config.Settings;
import dev.rocketatmosphere.space.SpaceDetector;
import dev.rocketatmosphere.space.SpaceState;
import net.minecraft.client.Minecraft;
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
 *   /rocketatmosphere space                      -> estado do detector de espaco na sua posicao
 *   /rocketatmosphere space altitude &lt;valor&gt;     -> altitude do detector simples
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
                .then(Commands.literal("space")
                        .executes(RACommands::space)
                        .then(Commands.literal("altitude")
                                .then(Commands.argument("value", DoubleArgumentType.doubleArg(100.0, 100000.0))
                                        .executes(RACommands::setAltitude))))
                .then(Commands.literal("status").executes(RACommands::status)));
    }

    private static int setQuality(CommandContext<CommandSourceStack> ctx, QualityPreset preset) {
        RAConfig.QUALITY.set(preset);
        RAConfig.QUALITY.save();
        ThrusterTracker.refreshSettings();
        ctx.getSource().sendSuccess(() -> Component.literal("[Rocket Atmosphere] qualidade: " + preset.name()), false);
        return 1;
    }

    private static int setAltitude(CommandContext<CommandSourceStack> ctx) {
        double v = DoubleArgumentType.getDouble(ctx, "value");
        RAConfig.FIXED_ALTITUDE.set(v);
        RAConfig.FIXED_ALTITUDE.save();
        ThrusterTracker.refreshSettings();
        ctx.getSource().sendSuccess(() -> Component.literal("[Rocket Atmosphere] altitude do detector simples: " + String.format("%.0f", v)), false);
        return 1;
    }

    private static int space(CommandContext<CommandSourceStack> ctx) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return 0;
        Settings cfg = ThrusterTracker.settings();
        SpaceState s = SpaceDetector.sample(mc.level, mc.player.getX(), mc.player.getY(), mc.player.getZ(), cfg);
        ctx.getSource().sendSuccess(() -> Component.literal(s.describe(cfg)), false);
        return 1;
    }

    private static int status(CommandContext<CommandSourceStack> ctx) {
        String msg = "[Rocket Atmosphere] qualidade=" + ThrusterTracker.settings().preset.name()
                + " | gancho ativo=" + (ThrusterTracker.hookSeen() ? "sim" : "ainda nao (nenhum motor visto)")
                + " | motores ativos rastreados=" + ThrusterTracker.trackedCount()
                + " | no espaco=" + ThrusterTracker.inSpaceCount()
                + " | vibracao=" + String.format("%.2f", CameraShake.intensity())
                + (ThrusterTracker.failed() ? " | DESATIVADO por erros (veja latest.log)" : "");
        ctx.getSource().sendSuccess(() -> Component.literal(msg), false);
        return 1;
    }
}

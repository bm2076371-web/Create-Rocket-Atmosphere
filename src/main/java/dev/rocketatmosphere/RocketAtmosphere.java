package dev.rocketatmosphere;

import com.mojang.logging.LogUtils;
import dev.rocketatmosphere.config.RAConfig;
import dev.rocketatmosphere.engine.RocketRegistries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;

/** Create: Rocket Atmosphere - efeitos de foguete, detector de espaco e motor Raptor 3 para o Create Propulsion. */
@Mod(RocketAtmosphere.MOD_ID)
public final class RocketAtmosphere {
    public static final String MOD_ID = "rocketatmosphere";
    public static final Logger LOGGER = LogUtils.getLogger();

    public RocketAtmosphere(IEventBus modBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.CLIENT, RAConfig.SPEC, "rocketatmosphere-client.toml");
        RocketRegistries.register(modBus);                 // motores + fluidos (comum: cliente e servidor)
        LOGGER.info("[RocketAtmosphere] carregado.");
    }
}

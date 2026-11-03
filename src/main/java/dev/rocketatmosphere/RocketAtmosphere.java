package dev.rocketatmosphere;

import com.mojang.logging.LogUtils;
import dev.rocketatmosphere.config.RAConfig;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;

/** Create: Rocket Atmosphere - efeitos visuais de foguete para o Create Propulsion (somente cliente). */
@Mod(RocketAtmosphere.MOD_ID)
public final class RocketAtmosphere {
    public static final String MOD_ID = "rocketatmosphere";
    public static final Logger LOGGER = LogUtils.getLogger();

    public RocketAtmosphere(IEventBus modBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.CLIENT, RAConfig.SPEC, "rocketatmosphere-client.toml");
        LOGGER.info("[RocketAtmosphere] carregado (efeitos somente no cliente).");
    }
}

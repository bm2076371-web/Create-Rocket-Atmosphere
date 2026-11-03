package dev.rocketatmosphere.mixin;

import dev.propulsionteam.propulsionsimulated.content.thruster.AbstractThrusterBlockEntity;
import dev.rocketatmosphere.client.ThrusterTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Gancho minimo e somente-leitura: ThrusterSoundHooks.clientTick(AbstractThrusterBlockEntity) e chamado
 * pelo Create Propulsion uma vez por tick, no cliente, para cada motor carregado (verificado em
 * AbstractThrusterBlockEntity.tick). Nenhum arquivo do Propulsion e alterado em disco; require=0 evita
 * crash caso uma versao futura do Propulsion renomeie o metodo (os efeitos apenas deixam de aparecer).
 */
@Mixin(targets = "dev.propulsionteam.propulsionsimulated.content.thruster.ThrusterSoundHooks")
public abstract class ThrusterSoundHooksMixin {

    @Inject(method = "clientTick", at = @At("HEAD"), require = 0)
    private static void rocketatmosphere$onClientTick(AbstractThrusterBlockEntity thruster, CallbackInfo ci) {
        ThrusterTracker.onThrusterTick(thruster);
    }
}

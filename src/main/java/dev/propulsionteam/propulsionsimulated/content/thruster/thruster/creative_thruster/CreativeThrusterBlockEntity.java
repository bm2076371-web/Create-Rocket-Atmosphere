package dev.propulsionteam.propulsionsimulated.content.thruster.thruster.creative_thruster;

import dev.propulsionteam.propulsionsimulated.content.thruster.AbstractThrusterBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/** STUB DE COMPILACAO: so o enum aninhado PlumeType (constantes copiadas do jar real). Nao empacotado. */
public abstract class CreativeThrusterBlockEntity extends AbstractThrusterBlockEntity {
    protected CreativeThrusterBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public enum PlumeType { PLASMA, ION, PLUME, NONE }
}

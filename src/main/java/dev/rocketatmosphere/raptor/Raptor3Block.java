package dev.rocketatmosphere.raptor;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntityTicker;
import dev.propulsionteam.propulsionsimulated.content.thruster.AbstractThrusterBlockEntity;
import dev.propulsionteam.propulsionsimulated.content.thruster.thruster.ThrusterBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Raptor 3: herda TUDO do ThrusterBlock do Create Propulsion (propriedade FACING + multi, hitbox, interacao,
 * chave inglesa, redstone). So troca o que o ThrusterBlock tem fixo para o BlockEntityType dele:
 * getBlockEntityType, newBlockEntity e getTicker (o getTicker original compara com o tipo do Thruster).
 */
public class Raptor3Block extends ThrusterBlock {
    public Raptor3Block(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntityType<? extends AbstractThrusterBlockEntity> getBlockEntityType() {
        return RaptorRegistries.RAPTOR_3_BE.get();
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new Raptor3BlockEntity(pos, state);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (type == (BlockEntityType<?>) RaptorRegistries.RAPTOR_3_BE.get()) {
            return (BlockEntityTicker<T>) (BlockEntityTicker<?>) new SmartBlockEntityTicker<SmartBlockEntity>();
        }
        return null;
    }
}

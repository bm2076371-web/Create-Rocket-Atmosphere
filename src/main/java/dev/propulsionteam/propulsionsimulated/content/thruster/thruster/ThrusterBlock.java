package dev.propulsionteam.propulsionsimulated.content.thruster.thruster;

import com.mojang.serialization.MapCodec;
import dev.propulsionteam.propulsionsimulated.content.thruster.AbstractThrusterBlock;
import dev.propulsionteam.propulsionsimulated.content.thruster.AbstractThrusterBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/** STUB DE COMPILACAO (assinaturas do javap). Nao empacotado. */
public class ThrusterBlock extends AbstractThrusterBlock {
    public static final MapCodec<ThrusterBlock> CODEC = simpleCodec(ThrusterBlock::new);

    public ThrusterBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends DirectionalBlock> codec() { return CODEC; }

    @Override
    public Class<AbstractThrusterBlockEntity> getBlockEntityClass() { return AbstractThrusterBlockEntity.class; }

    @Override
    public BlockEntityType<? extends AbstractThrusterBlockEntity> getBlockEntityType() { throw new UnsupportedOperationException("stub"); }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { throw new UnsupportedOperationException("stub"); }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        throw new UnsupportedOperationException("stub");
    }
}

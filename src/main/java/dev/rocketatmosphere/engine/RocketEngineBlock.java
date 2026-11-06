package dev.rocketatmosphere.engine;

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
 * Bloco de motor de foguete: herda TUDO do ThrusterBlock do Create Propulsion (FACING + multi, hitbox, interacao,
 * chave inglesa, redstone). So troca o que o ThrusterBlock tem fixo para o BlockEntityType dele:
 * getBlockEntityType, newBlockEntity e getTicker (o getTicker original compara com o tipo do Thruster).
 */
public class RocketEngineBlock extends ThrusterBlock {
    private final RocketEngineSpec spec;

    public RocketEngineBlock(RocketEngineSpec spec, BlockBehaviour.Properties properties) {
        super(properties);
        this.spec = spec;
    }

    public RocketEngineSpec spec() {
        return spec;
    }

    @Override
    public BlockEntityType<? extends AbstractThrusterBlockEntity> getBlockEntityType() {
        return RocketRegistries.ENGINE_BE.get();
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RocketEngineBlockEntity(pos, state);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (type == (BlockEntityType<?>) RocketRegistries.ENGINE_BE.get()) {
            return (BlockEntityTicker<T>) (BlockEntityTicker<?>) new SmartBlockEntityTicker<SmartBlockEntity>();
        }
        return null;
    }
}

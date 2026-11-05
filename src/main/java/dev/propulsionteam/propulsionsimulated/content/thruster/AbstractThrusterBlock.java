package dev.propulsionteam.propulsionsimulated.content.thruster;

import com.simibubi.create.foundation.block.IBE;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

/** STUB DE COMPILACAO. Nao empacotado. */
public abstract class AbstractThrusterBlock extends DirectionalBlock implements IBE<AbstractThrusterBlockEntity> {
    protected AbstractThrusterBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }
}

package dev.propulsionteam.propulsionsimulated.content.thruster;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import dev.propulsionteam.propulsionsimulated.content.thruster.thruster.creative_thruster.CreativeThrusterBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * STUB DE COMPILACAO. Assinaturas copiadas (javap) de createpropulsion-1.1.5.jar.
 * Nao e empacotado no jar final; em runtime vale a classe real do Create Propulsion.
 */
public abstract class AbstractThrusterBlockEntity extends SmartBlockEntity {
    public int width;

    protected AbstractThrusterBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public float getEffectiveThrottle() { throw new UnsupportedOperationException("stub"); }
    public boolean isVisuallyActive() { throw new UnsupportedOperationException("stub"); }
    public boolean isMultiblock() { throw new UnsupportedOperationException("stub"); }
    public boolean isController() { throw new UnsupportedOperationException("stub"); }
    public boolean isIon() { throw new UnsupportedOperationException("stub"); }
    public boolean isCreative() { throw new UnsupportedOperationException("stub"); }
    public CreativeThrusterBlockEntity.PlumeType getPlumeType() { throw new UnsupportedOperationException("stub"); }
    public WorldExhaustRay getWorldExhaustRay() { throw new UnsupportedOperationException("stub"); }
    public Direction getFacing() { throw new UnsupportedOperationException("stub"); }

    protected double calculateAtmosphericFactor() { throw new UnsupportedOperationException("stub"); }
    protected void setThrustAndSync(float thrust) { throw new UnsupportedOperationException("stub"); }

    public record WorldExhaustRay(Level level, Vec3 nozzlePos, Vec3 direction) {}
}

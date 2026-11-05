package dev.propulsionteam.propulsionsimulated.content.thruster.thruster;

import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import dev.propulsionteam.propulsionsimulated.content.thruster.AbstractThrusterBlockEntity;
import dev.propulsionteam.propulsionsimulated.content.thruster.FluidThrusterProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import java.util.List;

/** STUB DE COMPILACAO (membros e visibilidade copiados do javap do jar real). Nao empacotado. */
public class ThrusterBlockEntity extends AbstractThrusterBlockEntity {
    public SmartFluidTankBehaviour tank;
    public SmartFluidTankBehaviour oxidizerTank;
    protected double lastConsumedMbPerTick;

    public ThrusterBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) { throw new UnsupportedOperationException("stub"); }

    public FluidStack fluidStack() { throw new UnsupportedOperationException("stub"); }
    public boolean validFluid() { throw new UnsupportedOperationException("stub"); }
    protected boolean validOxidizer() { throw new UnsupportedOperationException("stub"); }
    protected boolean isWorking() { throw new UnsupportedOperationException("stub"); }
    protected boolean supportsMultiblock() { throw new UnsupportedOperationException("stub"); }
    protected int getBaseTankCapacityMb() { throw new UnsupportedOperationException("stub"); }
    protected double getBaseThrust() { throw new UnsupportedOperationException("stub"); }
    protected double getRawThrustCap() { throw new UnsupportedOperationException("stub"); }
    public double getNozzleOffsetFromCenter() { throw new UnsupportedOperationException("stub"); }
    public FluidThrusterProperties getFuelProperties(Fluid fluid) { throw new UnsupportedOperationException("stub"); }
    protected void updateSingleThrust(BlockState state) { throw new UnsupportedOperationException("stub"); }
    public IFluidHandler getFluidHandler(Direction side) { throw new UnsupportedOperationException("stub"); }
    protected void addThrusterDetails(List<Component> tooltip, boolean isPlayerSneaking) { throw new UnsupportedOperationException("stub"); }
}

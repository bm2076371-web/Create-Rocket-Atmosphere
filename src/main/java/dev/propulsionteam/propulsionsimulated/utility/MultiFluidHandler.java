package dev.propulsionteam.propulsionsimulated.utility;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

/** STUB DE COMPILACAO (ctor e metodos do javap). Nao empacotado. */
public class MultiFluidHandler implements IFluidHandler {
    public MultiFluidHandler(IFluidHandler handler1, IFluidHandler handler2) { }
    @Override public int getTanks() { return 0; }
    @Override public FluidStack getFluidInTank(int tank) { return FluidStack.EMPTY; }
    @Override public int getTankCapacity(int tank) { return 0; }
    @Override public boolean isFluidValid(int tank, FluidStack stack) { return false; }
    @Override public int fill(FluidStack resource, FluidAction action) { return 0; }
    @Override public FluidStack drain(FluidStack resource, FluidAction action) { return FluidStack.EMPTY; }
    @Override public FluidStack drain(int maxDrain, FluidAction action) { return FluidStack.EMPTY; }
}

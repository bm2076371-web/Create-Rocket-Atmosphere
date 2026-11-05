package dev.rocketatmosphere.raptor;

import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import dev.propulsionteam.propulsionsimulated.content.thruster.FluidThrusterProperties;
import dev.propulsionteam.propulsionsimulated.content.thruster.ThrusterParticleType;
import dev.propulsionteam.propulsionsimulated.content.thruster.thruster.ThrusterBlockEntity;
import dev.propulsionteam.propulsionsimulated.utility.MultiFluidHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import java.util.List;

/**
 * Raptor 3 = motor do Create Propulsion (ThrusterBlockEntity) com DOIS propelentes obrigatorios:
 *  - tank          -> metano liquido (CH4)
 *  - oxidizerTank  -> oxigenio liquido (LOX)
 *
 * Reaproveitado do Propulsion sem alteracao: empuxo na estrutura inteira (sable$physicsTick ->
 * SimulatedThrustAdapter.applyImpulseAtPoint), controle de potencia por redstone 0-15 = 0-100% (getPower /
 * getEffectiveThrottle, com rampa de partida), pluma, som, obstrucao, goggles e sincronizacao.
 *
 * O que o Raptor muda:
 *  - isWorking(): so funciona com metano E LOX; faltou um, o Propulsion zera o empuxo no proximo update.
 *  - calculateAtmosphericFactor(): 1.0, o motor nao depende do ar (o LOX vai a bordo): funciona no vacuo.
 *  - consumo de LOX proporcional ao metano queimado (razao volumetrica O/F).
 */
public class Raptor3BlockEntity extends ThrusterBlockEntity {
    public static final int TANK_CAPACITY_MB = 6000;
    /** Empuxo e consumo relativos ao Thruster comum (ponto de ajuste de balanceamento). */
    public static final double THRUST_MULTIPLIER = 4.0;
    /** mB de LOX por mB de metano. Raptor: O/F ~3,6 em massa; LOX 1141 kg/m3, CH4 liquido 422 kg/m3 => ~1,33 em volume. */
    public static final double LOX_PER_METHANE = 1.33;

    private static final FluidThrusterProperties PROPERTIES = new FluidThrusterProperties(
            1.0f, (float) THRUST_MULTIPLIER, ThrusterParticleType.PLUME, List.<ResourceLocation>of(), null, false);

    private double loxDebt;
    private IFluidHandler combinedHandler;

    public Raptor3BlockEntity(BlockPos pos, BlockState state) {
        super(RaptorRegistries.RAPTOR_3_BE.get(), pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        super.addBehaviours(behaviours);                 // cria `tank` (combustivel). O oxidizerTank do Propulsion so existe em multiblock.
        tank.getPrimaryHandler().setValidator(RaptorFluids::isMethaneStack);
        oxidizerTank = SmartFluidTankBehaviour.single(this, getBaseTankCapacityMb());
        behaviours.add(oxidizerTank);
        oxidizerTank.getPrimaryHandler().setValidator(RaptorFluids::isLoxStack);
    }

    @Override
    protected boolean supportsMultiblock() {
        return false;
    }

    @Override
    protected int getBaseTankCapacityMb() {
        return TANK_CAPACITY_MB;
    }

    @Override
    public boolean validFluid() {
        return RaptorFluids.isMethaneStack(fluidStack());
    }

    @Override
    protected boolean validOxidizer() {
        return oxidizerTank != null
                && RaptorFluids.isLoxStack(oxidizerTank.getPrimaryHandler().getFluid())
                && oxidizerTank.getPrimaryHandler().getFluidAmount() > 0;
    }

    /** Desligamento automatico: precisa dos dois propelentes. */
    @Override
    protected boolean isWorking() {
        return validFluid() && validOxidizer();
    }

    /** Sem dependencia de atmosfera: empuxo pleno no vacuo. */
    @Override
    protected double calculateAtmosphericFactor() {
        return 1.0;
    }

    @Override
    protected double getBaseThrust() {
        return super.getBaseThrust() * THRUST_MULTIPLIER;
    }

    @Override
    protected double getRawThrustCap() {
        return super.getRawThrustCap() * THRUST_MULTIPLIER;
    }

    /** Boca do bocal: o modelo vai de z=0 a z=32 (2 blocos), centro do bloco em z=8 => 1,5 bloco. */
    @Override
    public double getNozzleOffsetFromCenter() {
        return 1.5;
    }

    @Override
    public FluidThrusterProperties getFuelProperties(Fluid fluid) {
        return RaptorFluids.isMethaneFluid(fluid) ? PROPERTIES : null;
    }

    /** O Propulsion consome o metano e calcula o empuxo; aqui cobramos o LOX proporcional ao metano queimado. */
    @Override
    protected void updateSingleThrust(BlockState state) {
        int before = fuelAmount();
        super.updateSingleThrust(state);
        int burned = before - fuelAmount();
        if (burned <= 0) return;

        loxDebt += burned * LOX_PER_METHANE;
        int need = (int) loxDebt;
        if (need <= 0) return;
        FluidStack got = oxidizerTank.getPrimaryHandler().drain(need, IFluidHandler.FluidAction.EXECUTE);
        loxDebt -= got.getAmount();
        if (got.getAmount() < need) {                    // acabou o LOX no meio da queima: desliga ja
            loxDebt = 0;
            setThrustAndSync(0.0f);
        }
    }

    private int fuelAmount() {
        return tank == null ? 0 : tank.getPrimaryHandler().getFluidAmount();
    }

    /** Entrada de metano e LOX por qualquer lado, exceto pelo bocal (lado do escape = oposto ao FACING). */
    @Override
    public IFluidHandler getFluidHandler(Direction side) {
        if (tank == null || oxidizerTank == null) return null;
        if (side != null && side == getFacing().getOpposite()) return null;
        if (combinedHandler == null) {
            combinedHandler = new MultiFluidHandler(tank.getPrimaryHandler(), oxidizerTank.getPrimaryHandler());
        }
        return combinedHandler;
    }

    @Override
    protected void addThrusterDetails(List<Component> tooltip, boolean isPlayerSneaking) {
        super.addThrusterDetails(tooltip, isPlayerSneaking);
        if (tank != null && oxidizerTank != null) {
            tooltip.add(Component.translatable("tooltip.rocketatmosphere.raptor.methane",
                    tank.getPrimaryHandler().getFluidAmount(), tank.getPrimaryHandler().getCapacity()));
            tooltip.add(Component.translatable("tooltip.rocketatmosphere.raptor.lox",
                    oxidizerTank.getPrimaryHandler().getFluidAmount(), oxidizerTank.getPrimaryHandler().getCapacity()));
        }
    }
}

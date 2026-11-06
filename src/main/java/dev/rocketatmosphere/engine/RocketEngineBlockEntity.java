package dev.rocketatmosphere.engine;

import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import dev.propulsionteam.propulsionsimulated.content.thruster.AbstractThrusterBlockEntity.WorldExhaustRay;
import dev.propulsionteam.propulsionsimulated.content.thruster.FluidThrusterProperties;
import dev.propulsionteam.propulsionsimulated.content.thruster.thruster.ThrusterBlockEntity;
import dev.propulsionteam.propulsionsimulated.utility.MultiFluidHandler;
import dev.rocketatmosphere.space.AirPressure;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import java.util.List;

/**
 * Motor bipropelente = ThrusterBlockEntity do Create Propulsion com DOIS propelentes obrigatorios:
 *  - tank          -> combustivel (metano, RP-1 ou hidrogenio liquido, conforme o RocketEngineSpec)
 *  - oxidizerTank  -> oxigenio liquido (LOX)
 *
 * Reaproveitado do Propulsion sem alteracao: empuxo na estrutura inteira (sable$physicsTick ->
 * SimulatedThrustAdapter.applyImpulseAtPoint), potencia por redstone 0-15 = 0-100% (getPower /
 * getEffectiveThrottle, com rampa de partida), pluma, som, obstrucao, goggles e sincronizacao.
 *
 * O que muda: isWorking() exige combustivel E LOX; o empuxo nao depende do ar (o oxidante vai a bordo), exceto nos
 * motores de vacuo (Raptor Vacuum, RL10), que perdem empuxo dentro da atmosfera; o LOX e cobrado em proporcao
 * ao combustivel queimado.
 *
 * ATENCAO: o construtor do SmartBlockEntity chama addBehaviours ANTES dos campos desta classe existirem, por isso o
 * spec e sempre lido do bloco (getBlockState) e nunca guardado em campo.
 */
public class RocketEngineBlockEntity extends ThrusterBlockEntity {
    private double oxidizerDebt;
    private IFluidHandler combinedHandler;

    public RocketEngineBlockEntity(BlockPos pos, BlockState state) {
        super(RocketRegistries.ENGINE_BE.get(), pos, state);
    }

    public RocketEngineSpec spec() {
        return ((RocketEngineBlock) getBlockState().getBlock()).spec();
    }

    public double smokeFactor() {
        return spec().smokeFactor();
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        super.addBehaviours(behaviours);                 // cria `tank`; o oxidizerTank do Propulsion so existe em multiblock
        RocketEngineSpec spec = spec();
        tank.getPrimaryHandler().setValidator(s -> RocketFluids.matches(s, spec.fuelTag()));
        oxidizerTank = SmartFluidTankBehaviour.single(this, getBaseTankCapacityMb());
        behaviours.add(oxidizerTank);
        oxidizerTank.getPrimaryHandler().setValidator(s -> RocketFluids.matches(s, spec.oxidizerTag()));
    }

    @Override
    protected boolean supportsMultiblock() {
        return false;
    }

    @Override
    protected int getBaseTankCapacityMb() {
        return spec().tankMb();
    }

    @Override
    public boolean validFluid() {
        return RocketFluids.matches(fluidStack(), spec().fuelTag());
    }

    @Override
    protected boolean validOxidizer() {
        return oxidizerTank != null
                && RocketFluids.matches(oxidizerTank.getPrimaryHandler().getFluid(), spec().oxidizerTag())
                && oxidizerTank.getPrimaryHandler().getFluidAmount() > 0;
    }

    /** Desligamento automatico: precisa dos dois propelentes. */
    @Override
    protected boolean isWorking() {
        return validFluid() && validOxidizer();
    }

    /**
     * Motores de nivel do mar: 1.0 (nao dependem de ar). Motores de vacuo: de seaLevelFactor (no ar) ate 1.0 (vacuo),
     * pela pressao do ar na boca do bocal (mesma fonte que o Propulsion usa: DimensionPhysicsData.getAirPressure).
     */
    @Override
    protected double calculateAtmosphericFactor() {
        RocketEngineSpec spec = spec();
        if (!spec.vacuumOptimized()) return 1.0;
        double pressure = Double.NaN;
        try {
            Level level = getLevel();
            WorldExhaustRay ray = getWorldExhaustRay();
            if (level != null && ray != null) {
                Vec3 p = ray.nozzlePos();
                pressure = AirPressure.sample(level, p.x, p.y, p.z);
            }
        } catch (Throwable ignored) {
            // sem pressao disponivel: trata como ar normal
        }
        if (Double.isNaN(pressure)) pressure = 1.0;
        return spec.seaLevelFactor() + (1.0 - spec.seaLevelFactor()) * (1.0 - pressure);
    }

    @Override
    protected double getBaseThrust() {
        return super.getBaseThrust() * spec().thrustMultiplier();
    }

    @Override
    protected double getRawThrustCap() {
        return super.getRawThrustCap() * spec().thrustMultiplier();
    }

    @Override
    public double getNozzleOffsetFromCenter() {
        return spec().nozzleOffset();
    }

    @Override
    public FluidThrusterProperties getFuelProperties(Fluid fluid) {
        RocketEngineSpec spec = spec();
        return fluid.is(spec.fuelTag()) ? spec.properties() : null;
    }

    /** O Propulsion consome o combustivel e calcula o empuxo; aqui cobramos o LOX proporcional ao combustivel queimado. */
    @Override
    protected void updateSingleThrust(BlockState state) {
        int before = fuelAmount();
        super.updateSingleThrust(state);
        int burned = before - fuelAmount();
        if (burned <= 0) return;

        oxidizerDebt += burned * spec().oxidizerPerFuel();
        int need = (int) oxidizerDebt;
        if (need <= 0) return;
        FluidStack got = oxidizerTank.getPrimaryHandler().drain(need, IFluidHandler.FluidAction.EXECUTE);
        oxidizerDebt -= got.getAmount();
        if (got.getAmount() < need) {                    // acabou o LOX no meio da queima: desliga ja
            oxidizerDebt = 0;
            setThrustAndSync(0.0f);
        }
    }

    private int fuelAmount() {
        return tank == null ? 0 : tank.getPrimaryHandler().getFluidAmount();
    }

    /** Entrada de combustivel e LOX por qualquer lado, exceto pelo bocal (lado do escape = oposto ao FACING). */
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
            RocketEngineSpec spec = spec();
            tooltip.add(Component.translatable("tooltip.rocketatmosphere.engine.propellant",
                    Component.translatable(spec.fuelNameKey()),
                    tank.getPrimaryHandler().getFluidAmount(), tank.getPrimaryHandler().getCapacity()));
            tooltip.add(Component.translatable("tooltip.rocketatmosphere.engine.propellant",
                    Component.translatable(spec.oxidizerNameKey()),
                    oxidizerTank.getPrimaryHandler().getFluidAmount(), oxidizerTank.getPrimaryHandler().getCapacity()));
        }
    }
}

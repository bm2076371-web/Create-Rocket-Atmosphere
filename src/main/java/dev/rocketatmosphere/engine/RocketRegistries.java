package dev.rocketatmosphere.engine;

import dev.rocketatmosphere.RocketAtmosphere;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Registros dos motores, fluidos e baldes. Mesmo estilo do Create Propulsion (DeferredRegister). */
public final class RocketRegistries {
    private static final String ID = RocketAtmosphere.MOD_ID;

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ID);
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ID);
    public static final DeferredRegister<FluidType> FLUID_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, ID);
    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(Registries.FLUID, ID);

    /** Um fluido completo: tipo, fonte, fluxo, bloco e balde. */
    public record RocketFluid(String name,
                              DeferredHolder<FluidType, FluidType> type,
                              DeferredHolder<Fluid, BaseFlowingFluid.Source> source,
                              DeferredHolder<Fluid, BaseFlowingFluid.Flowing> flowing,
                              DeferredBlock<LiquidBlock> block,
                              DeferredHolder<Item, BucketItem> bucket) {}

    public static final List<RocketFluid> FLUID_LIST = new ArrayList<>();

    // densidade/viscosidade aproximadas das reais (kg/m3)
    public static final RocketFluid METHANE = registerFluid("methane", 420, 200);
    public static final RocketFluid LOX = registerFluid("liquid_oxygen", 1141, 300);
    public static final RocketFluid RP1 = registerFluid("rp1", 810, 1800);
    public static final RocketFluid LH2 = registerFluid("liquid_hydrogen", 71, 100);

    // ---- motores (um bloco por motor, um unico tipo de BlockEntity para todos)
    public static final Map<String, DeferredBlock<RocketEngineBlock>> ENGINE_BLOCKS = new LinkedHashMap<>();
    public static final Map<String, DeferredItem<BlockItem>> ENGINE_ITEMS = new LinkedHashMap<>();

    static {
        for (RocketEngineSpec spec : RocketEngines.ALL) {
            DeferredBlock<RocketEngineBlock> block = BLOCKS.register(spec.id(),
                    () -> new RocketEngineBlock(spec, BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).noOcclusion()));
            ENGINE_BLOCKS.put(spec.id(), block);
            ENGINE_ITEMS.put(spec.id(), ITEMS.registerSimpleBlockItem(spec.id(), block));
        }
    }

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<RocketEngineBlockEntity>> ENGINE_BE =
            BLOCK_ENTITIES.register("rocket_engine", () -> BlockEntityType.Builder.of(RocketEngineBlockEntity::new,
                    ENGINE_BLOCKS.values().stream().map(DeferredBlock::get).toArray(Block[]::new)).build(null));

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB = TABS.register("main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.rocketatmosphere.main"))
                    .icon(() -> new ItemStack(ENGINE_ITEMS.get(RocketEngines.RAPTOR_3.id()).get()))
                    .displayItems((params, output) -> {
                        for (DeferredItem<BlockItem> item : ENGINE_ITEMS.values()) output.accept(item.get());
                        for (RocketFluid f : FLUID_LIST) output.accept(f.bucket().get());
                    })
                    .build());

    private RocketRegistries() {}

    private static RocketFluid registerFluid(String name, int density, int viscosity) {
        final BaseFlowingFluid.Properties[] props = new BaseFlowingFluid.Properties[1];   // quebra a referencia circular entre os holders
        DeferredHolder<FluidType, FluidType> type = FLUID_TYPES.register(name,
                () -> new FluidType(FluidType.Properties.create()
                        .descriptionId("fluid." + ID + "." + name).density(density).viscosity(viscosity)));
        DeferredHolder<Fluid, BaseFlowingFluid.Source> source = FLUIDS.register(name,
                () -> new BaseFlowingFluid.Source(props[0]));
        DeferredHolder<Fluid, BaseFlowingFluid.Flowing> flowing = FLUIDS.register("flowing_" + name,
                () -> new BaseFlowingFluid.Flowing(props[0]));
        DeferredBlock<LiquidBlock> block = BLOCKS.register(name,
                () -> new LiquidBlock(source.get(), BlockBehaviour.Properties.ofFullCopy(Blocks.WATER).noLootTable()));
        DeferredHolder<Item, BucketItem> bucket = ITEMS.register(name + "_bucket",
                () -> new BucketItem(source.get(), new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)));
        props[0] = new BaseFlowingFluid.Properties(type, source, flowing)
                .bucket(bucket).block(block).slopeFindDistance(3).levelDecreasePerBlock(2).tickRate(4);
        RocketFluid fluid = new RocketFluid(name, type, source, flowing, block, bucket);
        FLUID_LIST.add(fluid);
        return fluid;
    }

    public static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        BLOCK_ENTITIES.register(modBus);
        FLUID_TYPES.register(modBus);
        FLUIDS.register(modBus);
        TABS.register(modBus);
        modBus.addListener(RocketRegistries::registerCapabilities);
    }

    /** Mesma capability de fluidos que o Create Propulsion registra para os motores dele. */
    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, ENGINE_BE.get(), (be, side) -> be.getFluidHandler(side));
    }
}

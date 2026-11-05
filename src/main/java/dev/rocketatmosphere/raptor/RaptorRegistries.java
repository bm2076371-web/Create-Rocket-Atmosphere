package dev.rocketatmosphere.raptor;

import dev.rocketatmosphere.RocketAtmosphere;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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

/** Registros do Raptor 3 e dos dois fluidos (metano liquido e LOX). Mesmo estilo do Create Propulsion (DeferredRegister). */
public final class RaptorRegistries {
    private static final String ID = RocketAtmosphere.MOD_ID;

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ID);
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ID);
    public static final DeferredRegister<FluidType> FLUID_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, ID);
    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(Registries.FLUID, ID);

    // ---- motor
    public static final DeferredBlock<Raptor3Block> RAPTOR_3 = BLOCKS.register("raptor_3",
            () -> new Raptor3Block(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).noOcclusion()));
    public static final DeferredItem<net.minecraft.world.item.BlockItem> RAPTOR_3_ITEM = ITEMS.registerSimpleBlockItem("raptor_3", RAPTOR_3);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<Raptor3BlockEntity>> RAPTOR_3_BE = BLOCK_ENTITIES.register("raptor_3",
            () -> BlockEntityType.Builder.of(Raptor3BlockEntity::new, RAPTOR_3.get()).build(null));

    // ---- tipos de fluido (densidades/viscosidades aproximadas das reais, em kg/m3)
    public static final DeferredHolder<FluidType, FluidType> METHANE_TYPE = FLUID_TYPES.register("methane",
            () -> new FluidType(FluidType.Properties.create().descriptionId("fluid.rocketatmosphere.methane").density(420).viscosity(200)));
    public static final DeferredHolder<FluidType, FluidType> LOX_TYPE = FLUID_TYPES.register("liquid_oxygen",
            () -> new FluidType(FluidType.Properties.create().descriptionId("fluid.rocketatmosphere.liquid_oxygen").density(1141).viscosity(300)));

    // ---- fluidos
    public static final DeferredHolder<Fluid, BaseFlowingFluid.Source> METHANE = FLUIDS.register("methane",
            () -> new BaseFlowingFluid.Source(methaneProperties()));
    public static final DeferredHolder<Fluid, BaseFlowingFluid.Flowing> METHANE_FLOWING = FLUIDS.register("flowing_methane",
            () -> new BaseFlowingFluid.Flowing(methaneProperties()));
    public static final DeferredHolder<Fluid, BaseFlowingFluid.Source> LOX = FLUIDS.register("liquid_oxygen",
            () -> new BaseFlowingFluid.Source(loxProperties()));
    public static final DeferredHolder<Fluid, BaseFlowingFluid.Flowing> LOX_FLOWING = FLUIDS.register("flowing_liquid_oxygen",
            () -> new BaseFlowingFluid.Flowing(loxProperties()));

    // ---- blocos de fluido e baldes
    public static final DeferredBlock<LiquidBlock> METHANE_BLOCK = BLOCKS.register("methane",
            () -> new LiquidBlock(METHANE.get(), BlockBehaviour.Properties.ofFullCopy(Blocks.WATER).noLootTable()));
    public static final DeferredBlock<LiquidBlock> LOX_BLOCK = BLOCKS.register("liquid_oxygen",
            () -> new LiquidBlock(LOX.get(), BlockBehaviour.Properties.ofFullCopy(Blocks.WATER).noLootTable()));
    public static final DeferredHolder<Item, BucketItem> METHANE_BUCKET = ITEMS.register("methane_bucket",
            () -> new BucketItem(METHANE.get(), new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)));
    public static final DeferredHolder<Item, BucketItem> LOX_BUCKET = ITEMS.register("liquid_oxygen_bucket",
            () -> new BucketItem(LOX.get(), new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)));

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB = TABS.register("main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.rocketatmosphere.main"))
                    .icon(() -> new ItemStack(RAPTOR_3_ITEM.get()))
                    .displayItems((params, output) -> {
                        output.accept(RAPTOR_3_ITEM.get());
                        output.accept(METHANE_BUCKET.get());
                        output.accept(LOX_BUCKET.get());
                    })
                    .build());

    private RaptorRegistries() {}

    private static BaseFlowingFluid.Properties methaneProperties() {
        return new BaseFlowingFluid.Properties(METHANE_TYPE, METHANE, METHANE_FLOWING)
                .bucket(METHANE_BUCKET).block(METHANE_BLOCK)
                .slopeFindDistance(3).levelDecreasePerBlock(2).tickRate(4);
    }

    private static BaseFlowingFluid.Properties loxProperties() {
        return new BaseFlowingFluid.Properties(LOX_TYPE, LOX, LOX_FLOWING)
                .bucket(LOX_BUCKET).block(LOX_BLOCK)
                .slopeFindDistance(3).levelDecreasePerBlock(2).tickRate(4);
    }

    public static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        BLOCK_ENTITIES.register(modBus);
        FLUID_TYPES.register(modBus);
        FLUIDS.register(modBus);
        TABS.register(modBus);
        modBus.addListener(RaptorRegistries::registerCapabilities);
    }

    /** Mesma capability de fluidos que o Create Propulsion registra para os motores dele. */
    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, RAPTOR_3_BE.get(), (be, side) -> be.getFluidHandler(side));
    }
}

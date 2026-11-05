package com.simibubi.create.foundation.blockEntity;

import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/** STUB DE COMPILACAO (Create). Nao empacotado. */
public abstract class SmartBlockEntity extends BlockEntity {
    public SmartBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public abstract void addBehaviours(List<BlockEntityBehaviour> behaviours);
}

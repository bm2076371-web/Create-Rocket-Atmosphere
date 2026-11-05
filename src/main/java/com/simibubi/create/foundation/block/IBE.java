package com.simibubi.create.foundation.block;

import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

/** STUB DE COMPILACAO (Create). Nao empacotado. */
public interface IBE<T extends BlockEntity> extends EntityBlock {
    Class<T> getBlockEntityClass();

    BlockEntityType<? extends T> getBlockEntityType();
}

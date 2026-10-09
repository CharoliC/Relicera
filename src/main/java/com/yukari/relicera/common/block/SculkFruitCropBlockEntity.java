package com.yukari.relicera.common.block;

import com.yukari.relicera.config.ModCommonConfig;
import com.yukari.relicera.registry.ModBlockEntities;
import com.yukari.relicera.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class SculkFruitCropBlockEntity extends BlockEntity {
    private static final String GROWTH_PROGRESS_TAG = "GrowthProgress";
    private int growthProgress;

    public SculkFruitCropBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SCULK_FRUIT_CROP.get(), pos, state);
    }

    public int absorbCharge(int charge) {
        if (level == null || level.isClientSide || charge <= 0) {
            return charge;
        }
        BlockState state = level.getBlockState(worldPosition);
        if (!state.is(ModBlocks.SCULK_FRUIT_CROP.get())
                || state.getValue(SculkFruitCropBlock.AGE) == SculkFruitCropBlock.MAX_AGE) {
            return charge;
        }

        int age = state.getValue(SculkFruitCropBlock.AGE);
        int cost = ModCommonConfig.SCULK_FRUIT_EXPERIENCE_PER_STAGE.get();
        long progress = (long) growthProgress + charge;
        int stages = (int) Math.min(SculkFruitCropBlock.MAX_AGE - age, progress / cost);
        int newAge = age + stages;
        if (stages > 0 && !level.setBlock(worldPosition, state.setValue(SculkFruitCropBlock.AGE, newAge), Block.UPDATE_ALL)) {
            return charge;
        }

        long remainder = progress - (long) stages * cost;
        growthProgress = newAge == SculkFruitCropBlock.MAX_AGE ? 0 : (int) remainder;
        setChanged();
        if (stages > 0) {
            level.levelEvent(2005, worldPosition, 0);
        }
        return newAge == SculkFruitCropBlock.MAX_AGE ? (int) Math.min(Integer.MAX_VALUE, remainder) : 0;
    }

    public void resetGrowth() {
        growthProgress = 0;
        setChanged();
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        growthProgress = Math.max(0, tag.getInt(GROWTH_PROGRESS_TAG));
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt(GROWTH_PROGRESS_TAG, growthProgress);
    }
}

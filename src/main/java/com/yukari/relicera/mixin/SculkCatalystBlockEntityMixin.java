package com.yukari.relicera.mixin;

import com.yukari.relicera.common.block.SculkFruitCropBlock;
import com.yukari.relicera.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.SculkCatalystBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SculkCatalystBlockEntity.class)
public abstract class SculkCatalystBlockEntityMixin {
    @Inject(method = "serverTick", at = @At("HEAD"), cancellable = true)
    private static void relicera$pauseSculkSpread(Level level, BlockPos pos, BlockState state,
                                                SculkCatalystBlockEntity catalyst, CallbackInfo ci) {
        BlockState crop = level.getBlockState(pos.above());
        if (crop.is(ModBlocks.SCULK_FRUIT_CROP.get())
                && crop.getValue(SculkFruitCropBlock.AGE) < SculkFruitCropBlock.MAX_AGE) {
            ci.cancel();
        }
    }
}

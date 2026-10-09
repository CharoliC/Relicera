package com.yukari.relicera.common.item;

import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemNameBlockItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class SculkFruitSeedsItem extends ItemNameBlockItem {
    public SculkFruitSeedsItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (context.getClickedFace() != Direction.UP
                || !context.getLevel().getBlockState(context.getClickedPos()).is(Blocks.SCULK_CATALYST)
                || !context.getLevel().getFluidState(context.getClickedPos().above()).isEmpty()) {
            return InteractionResult.PASS;
        }
        return super.useOn(context);
    }
}

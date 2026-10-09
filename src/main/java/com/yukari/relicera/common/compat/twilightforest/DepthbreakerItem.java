package com.yukari.relicera.common.compat.twilightforest;

import com.yukari.relicera.ReliceraMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeTier;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.TierSortingRegistry;
import twilightforest.data.tags.BlockTagGenerator;
import twilightforest.item.MazebreakerPickItem;

public final class DepthbreakerItem extends MazebreakerPickItem {
    private static final float EXTRA_MINING_SPEED_MULTIPLIER = 16.0F;
    private static final Tier TIER = new ForgeTier(
            4, 2680, 8.0F, 4.0F, 10, Tags.Blocks.NEEDS_NETHERITE_TOOL, () -> Ingredient.EMPTY);
    private static final TagKey<Block> ACCELERATED_MINING = BlockTags.create(
            ResourceLocation.fromNamespaceAndPath(ReliceraMod.MOD_ID, "depthbreaker_accelerated_mining"));

    private DepthbreakerItem() {
        super(TIER, new Item.Properties().rarity(Rarity.RARE).setNoRepair());
    }

    public static Item create() {
        return new DepthbreakerItem();
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        if (state.is(Blocks.REINFORCED_DEEPSLATE)) {
            return TIER.getSpeed();
        }
        float speed = super.getDestroySpeed(stack, state);
        if (state.is(ACCELERATED_MINING) && !state.is(BlockTagGenerator.MAZEBREAKER_ACCELERATED)) {
            return speed * EXTRA_MINING_SPEED_MULTIPLIER;
        }
        return speed;
    }

    @Override
    public boolean isValidRepairItem(ItemStack stack, ItemStack repairIngredient) {
        return repairIngredient.is(Items.EMERALD);
    }

    @Override
    public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
        return state.is(Blocks.REINFORCED_DEEPSLATE)
                || state.is(BlockTags.MINEABLE_WITH_PICKAXE)
                && TierSortingRegistry.isCorrectTierForDrops(Tiers.NETHERITE, state);
    }
}

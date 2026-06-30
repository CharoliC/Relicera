package com.yukari.relicera.common.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import org.jetbrains.annotations.Nullable;

public class TorchflowerEmberItem extends Item {
    private static final int BURN_TIME_TICKS = 2400;

    public TorchflowerEmberItem(Properties properties) {
        super(properties);
    }

    @Override
    public int getBurnTime(ItemStack itemStack, @Nullable RecipeType<?> recipeType) {
        return BURN_TIME_TICKS;
    }
}

package com.yukari.relicera.common.tooltip;

import net.minecraft.core.NonNullList;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

public class IluthiasChaliceTooltip implements TooltipComponent {
    private final NonNullList<ItemStack> totems;
    private final int capacity;

    public IluthiasChaliceTooltip(NonNullList<ItemStack> totems, int capacity) {
        this.totems = totems;
        this.capacity = capacity;
    }

    public NonNullList<ItemStack> totems() {
        return totems;
    }

    public int capacity() {
        return capacity;
    }
}

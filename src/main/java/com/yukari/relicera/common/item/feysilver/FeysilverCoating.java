package com.yukari.relicera.common.item.feysilver;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public final class FeysilverCoating {
    private static final String COATING_KEY = "ReliceraFeysilverCoating";
    private static final String UNBREAKABLE_KEY = "Unbreakable";

    private FeysilverCoating() {
    }

    public static void apply(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putBoolean(UNBREAKABLE_KEY, true);
        tag.putBoolean(COATING_KEY, true);
    }

    public static boolean hasCoating(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.getBoolean(COATING_KEY);
    }

    public static boolean isUnbreakable(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.getBoolean(UNBREAKABLE_KEY);
    }
}

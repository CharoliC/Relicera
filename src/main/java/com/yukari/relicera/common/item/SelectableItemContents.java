package com.yukari.relicera.common.item;

import net.minecraft.world.item.ItemStack;

public interface SelectableItemContents {
    String SELECTED_SLOT_TAG = "ReliceraSelectedContentsSlot";

    int getContentsSlotCount();

    default int getSelectedContentsSlot(ItemStack stack) {
        int selected = stack.hasTag() ? stack.getTag().getInt(SELECTED_SLOT_TAG) : 0;
        return selected >= 0 && selected < getContentsSlotCount() ? selected : 0;
    }

    default void setSelectedContentsSlot(ItemStack stack, int selected) {
        if (selected >= 0 && selected < getContentsSlotCount()) {
            if (selected == 0) {
                stack.removeTagKey(SELECTED_SLOT_TAG);
            } else {
                stack.getOrCreateTag().putInt(SELECTED_SLOT_TAG, selected);
            }
        }
    }
}

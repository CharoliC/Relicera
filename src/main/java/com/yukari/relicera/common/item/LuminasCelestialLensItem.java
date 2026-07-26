package com.yukari.relicera.common.item;

import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;

public class LuminasCelestialLensItem extends RelicCurioItem {
    public LuminasCelestialLensItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
        return false;
    }
}

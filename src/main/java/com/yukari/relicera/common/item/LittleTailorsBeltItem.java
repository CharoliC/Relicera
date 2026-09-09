package com.yukari.relicera.common.item;

import com.yukari.relicera.common.curio.LittleTailorsBeltEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;

public class LittleTailorsBeltItem extends QuickEquipCurioItem {
    public LittleTailorsBeltItem(Properties properties) {
        super(properties);
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        LivingEntity wearer = slotContext.entity();
        if (!wearer.level().isClientSide && wearer.tickCount % 5 == 0) {
            LittleTailorsBeltEffects.attackNearbyPests(wearer);
        }
    }
}

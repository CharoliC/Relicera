package com.yukari.relicera.common.item;

import com.yukari.relicera.common.effect.ephemeralbloom.EphemeralBloomAbsorption;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;

public class EphemeralBloomPendantItem extends QuickEquipCurioItem {
    public EphemeralBloomPendantItem(Properties properties) {
        super(properties);
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        LivingEntity entity = slotContext.entity();
        if (entity.level().isClientSide || entity.tickCount % EphemeralBloomAbsorption.intervalTicks() != 0) {
            return;
        }
        EphemeralBloomAbsorption.grant(entity);
    }
}

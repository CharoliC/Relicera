package com.yukari.relicera.common.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

public class EphemeralBloomPendantItem extends Item implements ICurioItem {
    private static final int ABSORPTION_INTERVAL_TICKS = 120;
    private static final float ABSORPTION_PER_INTERVAL = 2.0F;
    private static final float MAX_ABSORPTION = 20.0F;

    public EphemeralBloomPendantItem(Properties properties) {
        super(properties);
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        LivingEntity entity = slotContext.entity();
        if (entity.level().isClientSide || entity.tickCount % ABSORPTION_INTERVAL_TICKS != 0) {
            return;
        }

        float currentAbsorption = entity.getAbsorptionAmount();
        if (currentAbsorption < MAX_ABSORPTION) {
            entity.setAbsorptionAmount(Math.min(MAX_ABSORPTION, currentAbsorption + ABSORPTION_PER_INTERVAL));
        }
    }
}

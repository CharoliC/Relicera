package com.yukari.relicera.common.item;

import com.yukari.relicera.config.ModCommonConfig;
import com.yukari.relicera.registry.ModItems;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDropsEvent;

public final class StormscaleDrops {
    private StormscaleDrops() {
    }

    public static void addElderGuardianThunderstormDrop(LivingDropsEvent event) {
        if (!(event.getEntity().level() instanceof ServerLevel level)
                || event.getEntity().getType() != EntityType.ELDER_GUARDIAN
                || !level.isThundering()
                || level.random.nextDouble() >= ModCommonConfig.STORMSCALE_ELDER_GUARDIAN_THUNDERSTORM_DROP_CHANCE.get()) {
            return;
        }

        ItemEntity itemEntity = new ItemEntity(
                level,
                event.getEntity().getX(),
                event.getEntity().getY() + 0.5D,
                event.getEntity().getZ(),
                new ItemStack(ModItems.STORMSCALE.get())
        );
        itemEntity.setDefaultPickUpDelay();
        itemEntity.setGlowingTag(true);
        event.getDrops().add(itemEntity);
    }
}

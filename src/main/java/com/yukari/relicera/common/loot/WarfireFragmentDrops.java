package com.yukari.relicera.common.loot;

import com.yukari.relicera.common.curio.TurncoatsMedalEffects;
import com.yukari.relicera.common.curio.VindicatorsMedalEffects;
import com.yukari.relicera.config.ModCommonConfig;
import com.yukari.relicera.registry.ModItems;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDeathEvent;

public final class WarfireFragmentDrops {
    private WarfireFragmentDrops() {
    }

    public static void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof IronGolem golem)
                || !(golem.level() instanceof ServerLevel level)
                || !isDuringRaid(level, golem)
                || !isEligibleKiller(event.getSource().getEntity())
                || level.random.nextDouble() >= ModCommonConfig.WARFIRE_FRAGMENT_IRON_GOLEM_DROP_CHANCE.get()) {
            return;
        }

        ItemEntity itemEntity = new ItemEntity(
                level,
                golem.getX(),
                golem.getY() + 0.5D,
                golem.getZ(),
                new ItemStack(ModItems.WARFIRE_FRAGMENT.get())
        );
        itemEntity.setDefaultPickUpDelay();
        level.addFreshEntity(itemEntity);
    }

    private static boolean isDuringRaid(ServerLevel level, IronGolem golem) {
        return level.getRaidAt(golem.blockPosition()) != null;
    }

    private static boolean isEligibleKiller(Entity entity) {
        if (entity == null) {
            return false;
        }

        if (entity instanceof LivingEntity livingEntity
                && (VindicatorsMedalEffects.isEquipped(livingEntity)
                || TurncoatsMedalEffects.isEquipped(livingEntity))) {
            return true;
        }

        return entity.getType().is(EntityTypeTags.RAIDERS) || entity.getType() == EntityType.VEX;
    }
}

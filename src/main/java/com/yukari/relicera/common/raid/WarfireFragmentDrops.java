package com.yukari.relicera.common.raid;

import com.yukari.relicera.config.ModCommonConfig;
import com.yukari.relicera.registry.ModItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.registries.ForgeRegistries;

public final class WarfireFragmentDrops {
    private WarfireFragmentDrops() {
    }

    public static void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof IronGolem golem)
                || !(golem.level() instanceof ServerLevel level)
                || !isDuringRaid(level, golem)
                || !isConfiguredKiller(event.getSource().getEntity())
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

    private static boolean isConfiguredKiller(Entity entity) {
        if (entity == null) {
            return false;
        }

        ResourceLocation entityId = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());
        return entityId != null
                && ModCommonConfig.WARFIRE_FRAGMENT_IRON_GOLEM_KILLER_ENTITY_TYPES.get().contains(entityId.toString());
    }
}

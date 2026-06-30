package com.yukari.relicera.common.raid;

import com.yukari.relicera.config.ModServerConfig;
import com.yukari.relicera.registry.ModItems;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDeathEvent;

import java.util.Set;

public final class WarfireFragmentDrops {
    private static final Set<EntityType<?>> ELIGIBLE_RAIDERS = Set.of(
            EntityType.RAVAGER,
            EntityType.VINDICATOR,
            EntityType.PILLAGER,
            EntityType.EVOKER
    );

    private WarfireFragmentDrops() {
    }

    public static void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof IronGolem golem)
                || !(golem.level() instanceof ServerLevel level)
                || !(event.getSource().getEntity() instanceof Raider raider)
                || !isEligibleRaider(raider)
                || level.random.nextDouble() >= ModServerConfig.WARFIRE_FRAGMENT_IRON_GOLEM_RAIDER_DROP_CHANCE.get()) {
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

    private static boolean isEligibleRaider(Entity entity) {
        return entity instanceof Raider raider
                && raider.hasActiveRaid()
                && ELIGIBLE_RAIDERS.contains(raider.getType());
    }
}

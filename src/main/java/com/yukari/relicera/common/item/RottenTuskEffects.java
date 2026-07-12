package com.yukari.relicera.common.item;

import com.yukari.relicera.common.curio.BrutalPlunderBadgeEffects;
import com.yukari.relicera.config.ModCommonConfig;
import com.yukari.relicera.registry.ModItems;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;

public final class RottenTuskEffects {
    private static final long PIGLIN_AVOID_MEMORY_TICKS = 40L;
    private static final int PIGLIN_REPEL_INTERVAL_TICKS = 10;

    private RottenTuskEffects() {
    }

    public static void addZoglinDrop(LivingDropsEvent event) {
        if (event.getEntity().getType() != EntityType.ZOGLIN) {
            return;
        }

        event.getDrops().add(new ItemEntity(
                event.getEntity().level(),
                event.getEntity().getX(),
                event.getEntity().getY(),
                event.getEntity().getZ(),
                new ItemStack(ModItems.ROTTEN_TUSK.get())
        ));
    }

    public static void repelPiglins(ServerPlayer player) {
        if (player.tickCount % PIGLIN_REPEL_INTERVAL_TICKS != 0 || !hasPiglinDeterrent(player)) {
            return;
        }

        double range = ModCommonConfig.ROTTEN_TUSK_PIGLIN_REPEL_RANGE.get();
        if (range <= 0.0D) {
            return;
        }

        player.level().getEntitiesOfClass(
                Piglin.class,
                player.getBoundingBox().inflate(range),
                piglin -> piglin.isAlive() && piglin.distanceToSqr(player) <= range * range
        ).forEach(piglin -> {
            piglin.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
            piglin.getBrain().eraseMemory(MemoryModuleType.ANGRY_AT);
            piglin.getBrain().setMemoryWithExpiry(MemoryModuleType.AVOID_TARGET, player, PIGLIN_AVOID_MEMORY_TICKS);
        });
    }

    public static void preventPiglinDamage(LivingAttackEvent event) {
        if (event.getEntity() instanceof Player player
                && hasPiglinDeterrent(player)
                && isPiglinDamage(event.getSource().getEntity(), event.getSource().getDirectEntity())) {
            event.setCanceled(true);
        }
    }

    public static boolean hasPiglinDeterrent(Player player) {
        return isHoldingRottenTusk(player) || BrutalPlunderBadgeEffects.isEquipped(player);
    }

    private static boolean isHoldingRottenTusk(Player player) {
        return player.getMainHandItem().is(ModItems.ROTTEN_TUSK.get())
                || player.getOffhandItem().is(ModItems.ROTTEN_TUSK.get());
    }

    private static boolean isPiglinDamage(Entity sourceEntity, Entity directEntity) {
        return sourceEntity instanceof Piglin || directEntity instanceof Piglin;
    }
}

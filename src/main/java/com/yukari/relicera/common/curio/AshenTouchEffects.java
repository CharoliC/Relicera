package com.yukari.relicera.common.curio;

import com.yukari.relicera.config.ModServerConfig;
import com.yukari.relicera.registry.ModItems;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import top.theillusivec4.curios.api.CuriosApi;

public final class AshenTouchEffects {
    private static final int SNAPSHOT_TTL_TICKS = 5;

    private static final Map<UUID, AttackSnapshot> ATTACK_SNAPSHOTS = new HashMap<>();
    private static final Map<ResourceKey<Level>, Set<UUID>> PENDING_FIRE_CLEAR = new HashMap<>();

    private AshenTouchEffects() {
    }

    public static boolean isEquipped(Player player) {
        return CuriosApi.getCuriosInventory(player)
                .resolve()
                .map(handler -> handler.isEquipped(ModItems.ASHEN_TOUCH.get()))
                .orElse(false);
    }

    public static void rememberPreAttackFireState(AttackEntityEvent event) {
        if (event.getEntity().level().isClientSide()
                || !(event.getTarget() instanceof LivingEntity target)
                || !isEquipped(event.getEntity())) {
            return;
        }

        ATTACK_SNAPSHOTS.put(target.getUUID(), new AttackSnapshot(
                event.getEntity().getUUID(),
                event.getEntity().level().dimension(),
                event.getEntity().level().getGameTime(),
                target.isOnFire()
        ));
    }

    public static void applyMeleeFireEffects(LivingHurtEvent event) {
        Player player = getEquippedMeleeAttacker(event.getSource().getEntity(), event.getSource().getDirectEntity());
        if (player == null) {
            return;
        }

        LivingEntity target = event.getEntity();
        queueFireClear(target);

        AttackSnapshot snapshot = ATTACK_SNAPSHOTS.get(target.getUUID());
        if (snapshot == null
                || !snapshot.attackerId().equals(player.getUUID())
                || !snapshot.dimension().equals(player.level().dimension())
                || player.level().getGameTime() - snapshot.gameTime() > SNAPSHOT_TTL_TICKS
                || !snapshot.wasOnFire()) {
            return;
        }

        float multiplier = 1.0F + ModServerConfig.ASHEN_TOUCH_BURNING_TARGET_DAMAGE_BONUS.get().floatValue();
        event.setAmount(event.getAmount() * multiplier);
    }

    public static void clearQueuedFires(ServerLevel level) {
        long gameTime = level.getGameTime();
        ATTACK_SNAPSHOTS.entrySet().removeIf(entry -> gameTime - entry.getValue().gameTime() > SNAPSHOT_TTL_TICKS);

        Set<UUID> entityIds = PENDING_FIRE_CLEAR.remove(level.dimension());
        if (entityIds == null) {
            return;
        }

        for (UUID entityId : entityIds) {
            Entity entity = level.getEntity(entityId);
            if (entity instanceof LivingEntity livingEntity) {
                livingEntity.clearFire();
            }
        }
    }

    private static void queueFireClear(LivingEntity target) {
        target.clearFire();
        PENDING_FIRE_CLEAR
                .computeIfAbsent(target.level().dimension(), dimension -> new HashSet<>())
                .add(target.getUUID());
    }

    private static Player getEquippedMeleeAttacker(Entity sourceEntity, Entity directEntity) {
        if (sourceEntity instanceof Player player && directEntity == player && isEquipped(player)) {
            return player;
        }
        return null;
    }

    private record AttackSnapshot(UUID attackerId, ResourceKey<Level> dimension, long gameTime, boolean wasOnFire) {
    }
}

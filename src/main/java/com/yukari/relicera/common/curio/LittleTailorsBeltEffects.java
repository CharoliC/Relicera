package com.yukari.relicera.common.curio;

import com.yukari.relicera.common.network.ModNetworking;
import com.yukari.relicera.common.network.packet.SyncLittleTailorSeenThroughPacket;
import com.yukari.relicera.config.ModCommonConfig;
import com.yukari.relicera.registry.ModItems;
import com.yukari.relicera.registry.ModTags;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.Tags;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.event.entity.living.LivingConversionEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.registries.ForgeRegistries;
import top.theillusivec4.curios.api.CuriosApi;

public final class LittleTailorsBeltEffects {
    private static final String SEEN_THROUGH_TAG = "ReliceraLittleTailorsBeltSeenThrough";
    private static final double PEST_ATTACK_RANGE = 4.0D;
    private static final List<PendingReveal> PENDING_REVEALS = new ArrayList<>();

    private LittleTailorsBeltEffects() {
    }

    public static boolean isEquipped(LivingEntity entity) {
        return CuriosApi.getCuriosInventory(entity)
                .resolve()
                .map(handler -> handler.isEquipped(ModItems.LITTLE_TAILORS_BELT.get()))
                .orElse(false);
    }

    public static boolean isAffectedHumanoid(LivingEntity entity) {
        return entity.getType().is(ModTags.HUMANOIDS) && !entity.getType().is(Tags.EntityTypes.BOSSES);
    }

    public static boolean hasSeenThrough(LivingEntity entity) {
        return entity.getPersistentData().getBoolean(SEEN_THROUGH_TAG);
    }

    public static void attackNearbyPests(LivingEntity wearer) {
        if (!(wearer.level() instanceof ServerLevel serverLevel) || !wearer.isAlive()) {
            return;
        }

        List<? extends String> configuredEntityTypes = ModCommonConfig.LITTLE_TAILORS_BELT_INSTANT_KILL_ENTITY_TYPES.get();
        if (configuredEntityTypes.isEmpty()) {
            return;
        }

        double rangeSqr = PEST_ATTACK_RANGE * PEST_ATTACK_RANGE;
        DamageSource damageSource = wearer instanceof Player player
                ? serverLevel.damageSources().playerAttack(player)
                : serverLevel.damageSources().mobAttack(wearer);
        for (LivingEntity target : serverLevel.getEntitiesOfClass(
                LivingEntity.class,
                wearer.getBoundingBox().inflate(PEST_ATTACK_RANGE),
                target -> target != wearer
                        && target.isAlive()
                        && target.distanceToSqr(wearer) <= rangeSqr
                        && isConfiguredPest(target, configuredEntityTypes)
        )) {
            attackPest(serverLevel, target, damageSource);
        }
    }

    public static void preventIntimidatedTarget(LivingChangeTargetEvent event) {
        if (!(event.getEntity() instanceof Mob mob)
                || mob.level().isClientSide()
                || !isIntimidated(mob)) {
            return;
        }

        LivingEntity newTarget = event.getNewTarget();
        if (newTarget != null && isEquipped(newTarget)) {
            event.setCanceled(true);
        }
    }

    public static boolean preventIntimidatedAttack(LivingAttackEvent event) {
        if (event.getEntity().level().isClientSide()
                || !(event.getSource().getEntity() instanceof Mob attacker)
                || !isIntimidated(attacker)
                || !isEquipped(event.getEntity())) {
            return false;
        }

        event.setCanceled(true);
        if (attacker.getTarget() == event.getEntity()) {
            attacker.setTarget(null);
        }
        return true;
    }

    public static void clearIntimidatedTarget(LivingEntity entity) {
        if (!(entity instanceof Mob mob)
                || mob.level().isClientSide()
                || mob.tickCount % 10 != 0
                || !isIntimidated(mob)) {
            return;
        }

        LivingEntity target = mob.getTarget();
        if (target != null && isEquipped(target)) {
            mob.setTarget(null);
        }
    }

    public static void rememberFailedOneShotCandidate(LivingDamageEvent event) {
        LivingEntity target = event.getEntity();
        if (!(target.level() instanceof ServerLevel serverLevel)
                || event.getAmount() <= 0.0F
                || !isAffectedHumanoid(target)
                || !(event.getSource().getEntity() instanceof LivingEntity attacker)
                || attacker == target
                || !isEquipped(attacker)) {
            return;
        }

        MinecraftServer server = serverLevel.getServer();
        PendingReveal pending = new PendingReveal(
                serverLevel.dimension(),
                target.getUUID(),
                attacker.getUUID(),
                server.getTickCount() + 1
        );
        if (!PENDING_REVEALS.contains(pending)) {
            PENDING_REVEALS.add(pending);
        }
    }

    public static void processPendingReveals(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        MinecraftServer server = event.getServer();
        int currentTick = server.getTickCount();
        Iterator<PendingReveal> iterator = PENDING_REVEALS.iterator();
        while (iterator.hasNext()) {
            PendingReveal pending = iterator.next();
            if (currentTick < pending.dueTick()) {
                continue;
            }
            iterator.remove();

            ServerLevel level = server.getLevel(pending.dimension());
            if (level == null || !(level.getEntity(pending.targetId()) instanceof LivingEntity target) || !target.isAlive()) {
                continue;
            }

            Entity attackerEntity = level.getEntity(pending.attackerId());
            LivingEntity attacker = attackerEntity instanceof LivingEntity living && living.isAlive() ? living : null;
            revealToNearbyHumanoids(level, target, attacker);
        }
    }

    public static void clearPendingReveals() {
        PENDING_REVEALS.clear();
    }

    public static void copySeenThrough(LivingConversionEvent.Post event) {
        if (hasSeenThrough(event.getEntity())) {
            LivingEntity outcome = event.getOutcome();
            outcome.getPersistentData().putBoolean(SEEN_THROUGH_TAG, true);
            ModNetworking.sendToTracking(new SyncLittleTailorSeenThroughPacket(outcome.getUUID()), outcome);
        }
    }

    public static void syncSeenThrough(PlayerEvent.StartTracking event) {
        if (event.getEntity() instanceof ServerPlayer player
                && event.getTarget() instanceof LivingEntity target
                && hasSeenThrough(target)) {
            ModNetworking.sendToPlayer(new SyncLittleTailorSeenThroughPacket(target.getUUID()), player);
        }
    }

    private static boolean isIntimidated(Mob mob) {
        return isAffectedHumanoid(mob) && !hasSeenThrough(mob);
    }

    private static boolean isConfiguredPest(LivingEntity entity, List<? extends String> configuredEntityTypes) {
        ResourceLocation entityId = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());
        return entityId != null && configuredEntityTypes.contains(entityId.toString());
    }

    private static void attackPest(ServerLevel level, LivingEntity target, DamageSource damageSource) {
        double x = target.getX();
        double y = target.getY(0.5D);
        double z = target.getZ();
        if (!target.hurt(damageSource, Float.MAX_VALUE) || target.isAlive()) {
            return;
        }

        level.sendParticles(ParticleTypes.SWEEP_ATTACK, x, y, z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        level.playSound(null, x, y, z, SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    private static void revealToNearbyHumanoids(ServerLevel level, LivingEntity victim, LivingEntity attacker) {
        if (victim instanceof Mob victimMob) {
            reveal(victimMob, attacker);
        }

        double range = ModCommonConfig.LITTLE_TAILORS_BELT_REACTION_RANGE.get();
        if (range <= 0.0D || attacker == null) {
            return;
        }

        double rangeSqr = range * range;
        for (Mob witness : level.getEntitiesOfClass(
                Mob.class,
                victim.getBoundingBox().inflate(range),
                witness -> witness != victim
                        && witness.distanceToSqr(victim) <= rangeSqr
                        && isAffectedHumanoid(witness)
                        && witness.getSensing().hasLineOfSight(attacker)
        )) {
            reveal(witness, attacker);
        }
    }

    private static void reveal(Mob mob, LivingEntity attacker) {
        if (!hasSeenThrough(mob)) {
            mob.getPersistentData().putBoolean(SEEN_THROUGH_TAG, true);
            ModNetworking.sendToTracking(new SyncLittleTailorSeenThroughPacket(mob.getUUID()), mob);
            if (mob.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(
                        ParticleTypes.ANGRY_VILLAGER,
                        mob.getX(),
                        mob.getEyeY() + 0.35D,
                        mob.getZ(),
                        3,
                        0.2D,
                        0.15D,
                        0.2D,
                        0.0D
                );
            }
        }

        if (attacker != null && mob instanceof Enemy && mob.canAttack(attacker)) {
            mob.setTarget(attacker);
        }
    }

    private record PendingReveal(
            ResourceKey<Level> dimension,
            UUID targetId,
            UUID attackerId,
            int dueTick
    ) {
    }
}

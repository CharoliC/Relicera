package com.yukari.relicera.common.curio;

import com.yukari.relicera.ReliceraMod;
import com.yukari.relicera.config.ModCommonConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ReliceraMod.MOD_ID)
public final class BaromsCovenantStoneCombatEffects {
    private static final String FIRE_PREVIOUS_TICK_TAG = "ReliceraCreeperCovenantWasBurning";
    private static final String EXPLOSION_READY_TICK_TAG = "ReliceraCreeperCovenantReadyTick";
    private static final String EXPLOSION_FUSE_TICKS_TAG = "ReliceraCreeperCovenantFuseTicks";
    private static final String SKELETON_ARROW_TAG = "ReliceraSkeletonCovenantArrow";
    private static final String ARROW_X_TAG = "ReliceraSkeletonCovenantArrowX";
    private static final String ARROW_Y_TAG = "ReliceraSkeletonCovenantArrowY";
    private static final String ARROW_Z_TAG = "ReliceraSkeletonCovenantArrowZ";
    private static final String ARROW_IMPACT_DISTANCE_TAG = "ReliceraSkeletonCovenantImpactDistance";
    private static final int CREEPER_COOLDOWN_TICKS = 600;
    private static final int CREEPER_FUSE_TICKS = 36;
    private static final ThreadLocal<LivingEntity> ACTIVE_EXPLOSION_OWNER = new ThreadLocal<>();

    private BaromsCovenantStoneCombatEffects() {
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onVillagerDeath(LivingDeathEvent event) {
        if (event.isCanceled() || !(event.getEntity() instanceof Villager villager)
                || !(villager.level() instanceof ServerLevel level)
                || !(event.getSource().getEntity() instanceof LivingEntity killer)
                || !BaromsCovenantStoneEffects.hasContract(killer, EntityType.ZOMBIE)
                || !ForgeEventFactory.canLivingConvert(villager, EntityType.ZOMBIE_VILLAGER, timer -> {})) {
            return;
        }

        ZombieVillager converted = villager.convertTo(EntityType.ZOMBIE_VILLAGER, false);
        if (converted == null) {
            return;
        }
        converted.finalizeSpawn(level, level.getCurrentDifficultyAt(converted.blockPosition()),
                MobSpawnType.CONVERSION, new Zombie.ZombieGroupData(false, true), null);
        converted.setVillagerData(villager.getVillagerData());
        converted.setGossips(villager.getGossips().store(NbtOps.INSTANCE));
        converted.setTradeOffers(villager.getOffers().createTag());
        converted.setVillagerXp(villager.getVillagerXp());
        ForgeEventFactory.onLivingConvert(villager, converted);
        BaromsCovenantStoneEffects.breakContractOnKill(killer, EntityType.VILLAGER);
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity wearer = event.getEntity();
        if (wearer.level().isClientSide()) {
            return;
        }
        CompoundTag data = wearer.getPersistentData();
        if (data.contains(EXPLOSION_FUSE_TICKS_TAG, Tag.TAG_INT)) {
            if (!wearer.isAlive()) {
                data.remove(EXPLOSION_FUSE_TICKS_TAG);
            } else if (data.getInt(EXPLOSION_FUSE_TICKS_TAG) > 1) {
                data.putInt(EXPLOSION_FUSE_TICKS_TAG, data.getInt(EXPLOSION_FUSE_TICKS_TAG) - 1);
            } else {
                data.remove(EXPLOSION_FUSE_TICKS_TAG);
                data.putLong(EXPLOSION_READY_TICK_TAG, wearer.level().getGameTime() + CREEPER_COOLDOWN_TICKS);
                explode(wearer);
            }
            return;
        }
        if (!BaromsCovenantStoneEffects.hasContract(wearer, EntityType.CREEPER)) {
            data.remove(FIRE_PREVIOUS_TICK_TAG);
            return;
        }

        boolean burning = wearer.isOnFire();
        boolean justIgnited = burning && !data.getBoolean(FIRE_PREVIOUS_TICK_TAG);
        data.putBoolean(FIRE_PREVIOUS_TICK_TAG, burning);
        if (!justIgnited || wearer.level().getGameTime() < data.getLong(EXPLOSION_READY_TICK_TAG)) {
            return;
        }

        data.putBoolean(FIRE_PREVIOUS_TICK_TAG, false);
        data.putInt(EXPLOSION_FUSE_TICKS_TAG, CREEPER_FUSE_TICKS);
        wearer.clearFire();
        wearer.level().playSound(null, wearer.getX(), wearer.getY(), wearer.getZ(),
                SoundEvents.CREEPER_PRIMED, SoundSource.PLAYERS, 1.0F, 0.5F);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onWearerDeath(LivingDeathEvent event) {
        if (!event.isCanceled()) {
            event.getEntity().getPersistentData().remove(EXPLOSION_FUSE_TICKS_TAG);
        }
    }

    private static void explode(LivingEntity wearer) {
        ACTIVE_EXPLOSION_OWNER.set(wearer);
        try {
            wearer.level().explode(wearer, wearer.getX(), wearer.getY(0.5D), wearer.getZ(),
                    3.0F, Level.ExplosionInteraction.NONE);
        } finally {
            ACTIVE_EXPLOSION_OWNER.remove();
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onExplosionDetonate(ExplosionEvent.Detonate event) {
        LivingEntity owner = ACTIVE_EXPLOSION_OWNER.get();
        if (owner != null && event.getExplosion().getDirectSourceEntity() == owner) {
            event.getAffectedEntities().remove(owner);
        }
    }

    @SubscribeEvent
    public static void onProjectileSpawn(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) {
            return;
        }
        Entity projectile = event.getEntity();
        if (projectile instanceof Snowball snowball
                && snowball.getOwner() instanceof LivingEntity owner
                && BaromsCovenantStoneEffects.hasContract(owner, EntityType.BLAZE)) {
            Vec3 movement = snowball.getDeltaMovement();
            if (movement.lengthSqr() <= 1.0E-6D) {
                return;
            }
            SmallFireball fireball = new SmallFireball(event.getLevel(), owner,
                    movement.x, movement.y, movement.z);
            fireball.setPos(snowball.getX(), snowball.getY(), snowball.getZ());
            fireball.setDeltaMovement(movement);
            event.setCanceled(true);
            event.getLevel().addFreshEntity(fireball);
        } else if (projectile instanceof AbstractArrow arrow
                && arrow.getOwner() instanceof LivingEntity owner
                && BaromsCovenantStoneEffects.hasContract(owner, EntityType.SKELETON)) {
            CompoundTag data = arrow.getPersistentData();
            data.putBoolean(SKELETON_ARROW_TAG, true);
            data.putDouble(ARROW_X_TAG, arrow.getX());
            data.putDouble(ARROW_Y_TAG, arrow.getY());
            data.putDouble(ARROW_Z_TAG, arrow.getZ());
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onArrowImpact(ProjectileImpactEvent event) {
        if (event.isCanceled() || event.getProjectile().level().isClientSide()
                || !(event.getProjectile() instanceof AbstractArrow arrow)
                || event.getRayTraceResult().getType() != HitResult.Type.ENTITY
                || event.getImpactResult() == ProjectileImpactEvent.ImpactResult.SKIP_ENTITY
                || event.getImpactResult() == ProjectileImpactEvent.ImpactResult.STOP_AT_CURRENT_NO_DAMAGE) {
            return;
        }
        CompoundTag data = arrow.getPersistentData();
        if (!data.getBoolean(SKELETON_ARROW_TAG)) {
            return;
        }
        Vec3 origin = new Vec3(data.getDouble(ARROW_X_TAG),
                data.getDouble(ARROW_Y_TAG), data.getDouble(ARROW_Z_TAG));
        data.putDouble(ARROW_IMPACT_DISTANCE_TAG,
                Math.sqrt(event.getRayTraceResult().getLocation().distanceToSqr(origin)));
    }

    @SubscribeEvent
    public static void onArrowHurt(LivingHurtEvent event) {
        if (event.getEntity().level().isClientSide() || event.getAmount() <= 0.0F
                || !(event.getSource().getDirectEntity() instanceof AbstractArrow arrow)) {
            return;
        }
        CompoundTag data = arrow.getPersistentData();
        if (!data.getBoolean(SKELETON_ARROW_TAG)) {
            return;
        }
        double distance = data.contains(ARROW_IMPACT_DISTANCE_TAG)
                ? data.getDouble(ARROW_IMPACT_DISTANCE_TAG)
                : Math.sqrt(arrow.distanceToSqr(data.getDouble(ARROW_X_TAG),
                        data.getDouble(ARROW_Y_TAG), data.getDouble(ARROW_Z_TAG)));
        double bonus = Math.min(0.50D, 0.02D * Math.max(0.0D, distance - 10.0D));
        event.setAmount((float) (event.getAmount() * (1.0D + bonus)));
    }

    @SubscribeEvent
    public static void onMeleeDamage(LivingDamageEvent event) {
        if (event.getEntity().level().isClientSide() || event.getAmount() <= 0.0F
                || !(event.getSource().getEntity() instanceof Player player)
                || event.getSource().getDirectEntity() != player
                || !BaromsCovenantStoneEffects.hasContract(player, EntityType.HUSK)
                || player.getRandom().nextDouble() >= ModCommonConfig.BAROM_HUSK_HUNGER_CHANCE.get()) {
            return;
        }
        player.getFoodData().eat(1, 0.0F);
    }
}

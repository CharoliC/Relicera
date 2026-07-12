package com.yukari.relicera.common.effect;

import com.yukari.relicera.config.ModCommonConfig;
import com.yukari.relicera.registry.ModEffects;
import com.yukari.relicera.registry.ModParticleTypes;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class TempestSprintEffects {
    private static final UUID MOVEMENT_SPEED_MODIFIER_ID = UUID.fromString("a44034ef-6d35-4289-a61e-80dca8414a49");
    private static final UUID KNOCKBACK_RESISTANCE_MODIFIER_ID = UUID.fromString("3be44c6d-3173-405b-8945-42296f1ebeb7");
    private static final int FULL_JUMP_THRESHOLD = 80;
    private static final int FULL_JUMP_STATE_MAX_AGE = 200;
    private static final int ELECTRIC_SHOCKWAVE_POINTS = 72;
    private static final int END_ROD_SHOCKWAVE_INTERVAL = 8;
    private static final double KNOCKBACK_RESISTANCE_BONUS = 1.0D;
    private static final double FALL_GRAVITY_BONUS = -0.1D;

    private static final Map<UUID, FullJumpState> FULL_JUMP_STATES = new HashMap<>();

    private TempestSprintEffects() {
    }

    public static void tickHorse(LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof Horse horse)) {
            return;
        }

        boolean active = hasTempestSprint(horse);
        syncVisualState(horse, active);
        applySpeedModifier(horse, active);
        applyKnockbackResistanceModifier(horse, active);
        if (active) {
            horse.setStanding(false);
            applyFallGravityBonus(horse);
        }
        if (active && !horse.level().isClientSide()) {
            shockCollidingMonsters(horse);
            tickFullJumpShockwave(horse);
        } else if (!active && !horse.level().isClientSide()) {
            FULL_JUMP_STATES.remove(horse.getUUID());
        }
    }

    public static boolean reduceDamage(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof Horse horse) || !hasTempestSprint(horse)) {
            return false;
        }

        double reduction = ModCommonConfig.TEMPEST_SPRINT_DAMAGE_REDUCTION.get();
        event.setAmount((float) (event.getAmount() * Math.max(0.0D, 1.0D - reduction)));
        return event.getAmount() <= 0.0F;
    }

    public static boolean hasTempestSprint(Horse horse) {
        return horse.hasEffect(ModEffects.TEMPEST_SPRINT.get());
    }

    public static boolean hasTempestSprintVisual(Horse horse) {
        if (horse instanceof TempestSprintVisualState visualState && visualState.relicera$isTempestSprintVisualActive()) {
            return true;
        }
        return hasTempestSprint(horse);
    }

    public static void rememberFullJump(Horse horse, int jumpPower) {
        if (jumpPower < FULL_JUMP_THRESHOLD || horse.level().isClientSide() || !hasTempestSprint(horse)) {
            return;
        }

        FULL_JUMP_STATES.put(horse.getUUID(), new FullJumpState());
    }

    private static void shockCollidingMonsters(Horse horse) {
        List<Entity> entities = horse.level().getEntities(horse, horse.getBoundingBox().inflate(0.12D),
                entity -> entity instanceof Monster && entity.isAlive() && entity.isPickable());
        if (entities.isEmpty()) {
            return;
        }

        float damage = ModCommonConfig.TEMPEST_SPRINT_COLLISION_DAMAGE.get().floatValue();
        double knockback = ModCommonConfig.TEMPEST_SPRINT_COLLISION_KNOCKBACK.get();
        for (Entity entity : entities) {
            if (!(entity instanceof LivingEntity target)
                    || !horse.getBoundingBox().intersects(target.getBoundingBox().inflate(0.08D))) {
                continue;
            }

            if (target.hurt(horse.damageSources().mobAttack(horse), damage)) {
                forceKnockback(horse, target, knockback);
                playImpactFeedback(horse, target);
            }
        }
    }

    private static void syncVisualState(Horse horse, boolean active) {
        if (!horse.level().isClientSide() && horse instanceof TempestSprintVisualState visualState) {
            visualState.relicera$setTempestSprintVisualActive(active);
        }
    }

    private static void tickFullJumpShockwave(Horse horse) {
        FullJumpState state = FULL_JUMP_STATES.get(horse.getUUID());
        if (state == null) {
            return;
        }

        if (++state.age > FULL_JUMP_STATE_MAX_AGE || !horse.isAlive()) {
            FULL_JUMP_STATES.remove(horse.getUUID());
            return;
        }

        if (!horse.onGround()) {
            state.airborne = true;
            return;
        }

        if (state.airborne) {
            FULL_JUMP_STATES.remove(horse.getUUID());
            triggerFullJumpShockwave(horse);
        }
    }

    private static void triggerFullJumpShockwave(Horse horse) {
        if (!(horse.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        double range = ModCommonConfig.TEMPEST_SPRINT_FULL_JUMP_SHOCKWAVE_RANGE.get();
        float damage = ModCommonConfig.TEMPEST_SPRINT_FULL_JUMP_SHOCKWAVE_DAMAGE.get().floatValue();
        double knockback = ModCommonConfig.TEMPEST_SPRINT_FULL_JUMP_SHOCKWAVE_KNOCKBACK.get();
        double rangeSqr = range * range;

        List<Monster> targets = serverLevel.getEntitiesOfClass(Monster.class, horse.getBoundingBox().inflate(range, 1.5D, range),
                monster -> monster.isAlive() && monster.distanceToSqr(horse) <= rangeSqr);
        for (Monster target : targets) {
            if (target.hurt(horse.damageSources().mobAttack(horse), damage)) {
                forceKnockback(horse, target, knockback);
            }
        }

        spawnShockwaveParticles(serverLevel, horse);
        playLandingShockwaveSound(serverLevel, horse.getX(), horse.getY(0.15D), horse.getZ());
    }

    private static void spawnShockwaveParticles(ServerLevel serverLevel, Horse horse) {
        double centerY = horse.getY(0.3D);
        RandomSource random = serverLevel.getRandom();
        serverLevel.sendParticles(ParticleTypes.EXPLOSION_EMITTER, horse.getX(), horse.getY(0.15D), horse.getZ(), 1, 0.0D, 0.0D, 0.0D, 0.0D);
        spawnRadialElectricShockwave(serverLevel, horse, centerY, random);
    }

    private static void spawnRadialElectricShockwave(ServerLevel serverLevel, Horse horse, double centerY, RandomSource random) {
        for (int i = 0; i < ELECTRIC_SHOCKWAVE_POINTS * 2; i++) {
            double speed = random.nextDouble() * 0.34D + 0.28D;
            double angle = Math.PI / ELECTRIC_SHOCKWAVE_POINTS * i;
            double x = horse.getX();
            double y = centerY + 0.25D * random.nextDouble() + 0.15D;
            double z = horse.getZ();
            double xSpeed = speed * Math.sin(angle);
            double ySpeed = random.nextDouble() * 0.06D;
            double zSpeed = speed * Math.cos(angle);
            serverLevel.sendParticles(
                    ModParticleTypes.ELECTRIC_SPARK.get(),
                    x,
                    y,
                    z,
                    0,
                    xSpeed,
                    ySpeed,
                    zSpeed,
                    1.0D
            );
            if (i % END_ROD_SHOCKWAVE_INTERVAL == 0) {
                serverLevel.sendParticles(
                        ParticleTypes.END_ROD,
                        x,
                        y,
                        z,
                        0,
                        xSpeed,
                        ySpeed,
                        zSpeed,
                        1.0D
                );
            }
        }
    }

    public static void forceKnockback(Entity source, LivingEntity target, double knockback) {
        Vec3 direction = target.position().subtract(source.position());
        if (direction.horizontalDistanceSqr() < 1.0E-4D) {
            Vec3 movement = source.getDeltaMovement();
            direction = new Vec3(movement.x, 0.0D, movement.z);
        }
        if (direction.horizontalDistanceSqr() < 1.0E-4D) {
            direction = source.getLookAngle();
        }

        Vec3 horizontal = new Vec3(direction.x, 0.0D, direction.z);
        if (horizontal.horizontalDistanceSqr() < 1.0E-4D) {
            return;
        }

        Vec3 normalized = horizontal.normalize();
        double verticalBoost = target.onGround() ? 0.35D : 0.12D;
        target.push(normalized.x * knockback, verticalBoost, normalized.z * knockback);
    }

    private static void playImpactFeedback(Horse horse, LivingEntity target) {
        if (!(horse.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        double x = target.getX();
        double y = target.getY(0.55D);
        double z = target.getZ();
        spawnCollisionSparks(serverLevel, horse, target, x, y, z);
        playCollisionImpactSound(serverLevel, x, y, z);
    }

    private static void spawnCollisionSparks(ServerLevel serverLevel, Horse horse, LivingEntity target, double x, double y, double z) {
        Vec3 direction = target.position().subtract(horse.position());
        Vec3 horizontal = new Vec3(direction.x, 0.0D, direction.z);
        if (horizontal.horizontalDistanceSqr() < 1.0E-4D) {
            horizontal = horse.getDeltaMovement();
        }
        if (horizontal.horizontalDistanceSqr() < 1.0E-4D) {
            horizontal = horse.getLookAngle();
        }
        Vec3 normal = new Vec3(horizontal.x, 0.0D, horizontal.z).normalize();
        RandomSource random = serverLevel.getRandom();
        serverLevel.sendParticles(ModParticleTypes.ELECTRIC_SPARK.get(), x, y, z, 4, 0.2D, 0.18D, 0.2D, 0.08D);
        serverLevel.sendParticles(ParticleTypes.ENCHANTED_HIT, x, y, z, 8, 0.28D, 0.28D, 0.28D, 0.05D);
        for (int i = 0; i < 2; i++) {
            double side = (random.nextDouble() - 0.5D) * 0.16D;
            double up = 0.02D + random.nextDouble() * 0.1D;
            serverLevel.sendParticles(ModParticleTypes.ELECTRIC_SPARK.get(), x, y, z, 0,
                    normal.x * (0.12D + random.nextDouble() * 0.12D) - normal.z * side,
                    up,
                    normal.z * (0.12D + random.nextDouble() * 0.12D) + normal.x * side,
                    1.0D);
        }
    }

    private static void playLandingShockwaveSound(ServerLevel serverLevel, double x, double y, double z) {
        serverLevel.playSound(null, x, y, z, SoundEvents.TRIDENT_THUNDER, SoundSource.NEUTRAL, 0.9F, 1.05F);
        serverLevel.playSound(null, x, y, z, SoundEvents.TOTEM_USE, SoundSource.NEUTRAL, 0.55F, 0.85F);
    }

    private static void playCollisionImpactSound(ServerLevel serverLevel, double x, double y, double z) {
        serverLevel.playSound(null, x, y, z, SoundEvents.TOTEM_USE, SoundSource.NEUTRAL, 0.35F, 1.55F);
    }

    private static void applySpeedModifier(Horse horse, boolean shouldApply) {
        AttributeInstance instance = horse.getAttribute(Attributes.MOVEMENT_SPEED);
        if (instance == null) {
            return;
        }

        AttributeModifier existing = instance.getModifier(MOVEMENT_SPEED_MODIFIER_ID);
        if (shouldApply && existing != null) {
            return;
        }

        if (existing != null) {
            instance.removeModifier(MOVEMENT_SPEED_MODIFIER_ID);
        }

        if (shouldApply) {
            instance.addTransientModifier(new AttributeModifier(
                    MOVEMENT_SPEED_MODIFIER_ID,
                    "Tempest Sprint movement speed",
                    ModCommonConfig.TEMPEST_SPRINT_SPEED_BONUS.get(),
                    AttributeModifier.Operation.MULTIPLY_TOTAL
            ));
        }
    }

    private static void applyKnockbackResistanceModifier(Horse horse, boolean shouldApply) {
        AttributeInstance instance = horse.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
        if (instance == null) {
            return;
        }

        AttributeModifier existing = instance.getModifier(KNOCKBACK_RESISTANCE_MODIFIER_ID);
        if (shouldApply && existing != null) {
            return;
        }

        if (existing != null) {
            instance.removeModifier(KNOCKBACK_RESISTANCE_MODIFIER_ID);
        }

        if (shouldApply) {
            instance.addTransientModifier(new AttributeModifier(
                    KNOCKBACK_RESISTANCE_MODIFIER_ID,
                    "Tempest Sprint knockback resistance",
                    KNOCKBACK_RESISTANCE_BONUS,
                    AttributeModifier.Operation.ADDITION
            ));
        }
    }

    private static void applyFallGravityBonus(Horse horse) {
        if (horse.onGround() || horse.isInWaterOrBubble() || horse.isInLava()) {
            return;
        }

        Vec3 movement = horse.getDeltaMovement();
        if (movement.y < 0.0D) {
            horse.setDeltaMovement(movement.add(0.0D, FALL_GRAVITY_BONUS, 0.0D));
        }
    }

    private static final class FullJumpState {
        private boolean airborne;
        private int age;
    }
}

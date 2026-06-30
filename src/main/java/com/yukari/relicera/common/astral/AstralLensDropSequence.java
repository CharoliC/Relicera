package com.yukari.relicera.common.astral;

import com.yukari.relicera.registry.ModItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public final class AstralLensDropSequence {
    private static final String SEQUENCE_KEY = "ReliceraAstralLensDropSequence";
    private static final String STATE_KEY = "State";
    private static final String TICKS_KEY = "Ticks";
    private static final int STATE_DESCENDING = 1;
    private static final int STATE_CONVERGING = 2;

    private static final double START_FORWARD_OFFSET = 2.5D;
    private static final double START_HEIGHT_OFFSET = 7.0D;
    private static final double DESCENT_SPEED = 0.1D;
    private static final int LOCKED_PICKUP_DELAY = 32767;
    private static final int PORTAL_CONVERGE_TICKS = 40;
    private static final int END_ROD_PARTICLES_PER_TICK = 1;
    private static final int PORTAL_PARTICLES_PER_TICK = 7;
    private static final double PORTAL_START_RADIUS = 1.2D;
    private static final double PORTAL_INWARD_SPEED = 0.5D;

    private AstralLensDropSequence() {
    }

    public static void spawn(ServerLevel level, ServerPlayer player) {
        Vec3 lookDirection = player.getLookAngle();
        Vec3 horizontalDirection = new Vec3(lookDirection.x, 0.0D, lookDirection.z);
        if (horizontalDirection.lengthSqr() < 1.0E-4D) {
            horizontalDirection = Vec3.directionFromRotation(0.0F, player.getYRot());
        }
        horizontalDirection = horizontalDirection.normalize();

        Vec3 spawnPosition = player.position()
                .add(horizontalDirection.scale(START_FORWARD_OFFSET))
                .add(0.0D, START_HEIGHT_OFFSET, 0.0D);

        ItemEntity astralLens = new ItemEntity(
                level,
                spawnPosition.x,
                spawnPosition.y,
                spawnPosition.z,
                createSequenceStack()
        );
        astralLens.setGlowingTag(true);
        astralLens.setThrower(player.getUUID());
        astralLens.setPickUpDelay(LOCKED_PICKUP_DELAY);
        astralLens.setNoGravity(true);
        setState(astralLens, STATE_DESCENDING);
        setTicks(astralLens, 0);
        level.addFreshEntity(astralLens);
    }

    public static void tick(ItemEntity itemEntity) {
        int state = getState(itemEntity);
        if (state == STATE_DESCENDING) {
            tickDescending(itemEntity);
            return;
        }
        if (state == STATE_CONVERGING) {
            tickConverging(itemEntity);
        }
    }

    private static ItemStack createSequenceStack() {
        ItemStack stack = new ItemStack(ModItems.ASTRAL_LENS.get());
        CompoundTag sequence = new CompoundTag();
        sequence.putInt(STATE_KEY, STATE_DESCENDING);
        stack.getOrCreateTag().put(SEQUENCE_KEY, sequence);
        return stack;
    }

    private static void tickDescending(ItemEntity itemEntity) {
        itemEntity.setPickUpDelay(LOCKED_PICKUP_DELAY);
        itemEntity.setNoGravity(true);
        itemEntity.setDeltaMovement(0.0D, -DESCENT_SPEED, 0.0D);

        if (itemEntity.level() instanceof ServerLevel level) {
            level.sendParticles(
                    ParticleTypes.END_ROD,
                    itemEntity.getX(),
                    itemEntity.getY() + 0.25D,
                    itemEntity.getZ(),
                    END_ROD_PARTICLES_PER_TICK,
                    0.08D,
                    0.08D,
                    0.08D,
                    0.01D
            );
        }

        setTicks(itemEntity, getTicks(itemEntity) + 1);

        if (itemEntity.onGround()) {
            beginPortalConvergence(itemEntity);
        }
    }

    private static void beginPortalConvergence(ItemEntity itemEntity) {
        itemEntity.setNoGravity(false);
        itemEntity.setDeltaMovement(Vec3.ZERO);
        itemEntity.setPickUpDelay(LOCKED_PICKUP_DELAY);
        setState(itemEntity, STATE_CONVERGING);
        setTicks(itemEntity, 0);

        if (itemEntity.level() instanceof ServerLevel level) {
            level.sendParticles(
                    ParticleTypes.EXPLOSION,
                    itemEntity.getX(),
                    itemEntity.getY() + 0.25D,
                    itemEntity.getZ(),
                    1,
                    0.0D,
                    0.0D,
                    0.0D,
                    0.0D
            );
        }
    }

    private static void tickConverging(ItemEntity itemEntity) {
        itemEntity.setPickUpDelay(LOCKED_PICKUP_DELAY);
        itemEntity.setDeltaMovement(Vec3.ZERO);

        int ticks = getTicks(itemEntity);
        if (itemEntity.level() instanceof ServerLevel level) {
            spawnConvergingPortalParticles(level, itemEntity, ticks);
        }

        if (ticks >= PORTAL_CONVERGE_TICKS) {
            finish(itemEntity);
            return;
        }

        setTicks(itemEntity, ticks + 1);
    }

    private static void spawnConvergingPortalParticles(ServerLevel level, ItemEntity itemEntity, int ticks) {
        Vec3 center = itemEntity.position().add(0.0D, 0.35D, 0.0D);
        double progress = Mth.clamp(ticks / (double) PORTAL_CONVERGE_TICKS, 0.0D, 1.0D);
        double radius = Mth.lerp(progress, PORTAL_START_RADIUS, 0.45D);

        for (int i = 0; i < PORTAL_PARTICLES_PER_TICK; i++) {
            double theta = level.random.nextDouble() * Math.PI * 2.0D;
            double yOffset = (level.random.nextDouble() - 0.5D) * 1.4D;
            Vec3 offset = new Vec3(
                    Math.cos(theta) * radius,
                    yOffset,
                    Math.sin(theta) * radius
            );
            Vec3 particlePosition = center.add(offset);
            Vec3 velocity = center.subtract(particlePosition).normalize().scale(PORTAL_INWARD_SPEED);

            level.sendParticles(
                    ParticleTypes.PORTAL,
                    particlePosition.x,
                    particlePosition.y,
                    particlePosition.z,
                    0,
                    velocity.x,
                    velocity.y,
                    velocity.z,
                    1.0D
            );
        }
    }

    private static void finish(ItemEntity itemEntity) {
        clearSequence(itemEntity);
        itemEntity.setNoGravity(false);
        itemEntity.setDeltaMovement(Vec3.ZERO);
        itemEntity.setPickUpDelay(0);
    }

    private static int getState(ItemEntity itemEntity) {
        CompoundTag sequence = getStackSequence(itemEntity);
        if (!sequence.isEmpty()) {
            return sequence.getInt(STATE_KEY);
        }
        return itemEntity.getPersistentData().getCompound(SEQUENCE_KEY).getInt(STATE_KEY);
    }

    private static void setState(ItemEntity itemEntity, int state) {
        CompoundTag sequence = getOrCreateStackSequence(itemEntity);
        sequence.putInt(STATE_KEY, state);
        updateStackSequence(itemEntity, sequence);

        CompoundTag entitySequence = itemEntity.getPersistentData().getCompound(SEQUENCE_KEY);
        entitySequence.putInt(STATE_KEY, state);
        itemEntity.getPersistentData().put(SEQUENCE_KEY, entitySequence);
    }

    private static int getTicks(ItemEntity itemEntity) {
        return itemEntity.getPersistentData().getCompound(SEQUENCE_KEY).getInt(TICKS_KEY);
    }

    private static void setTicks(ItemEntity itemEntity, int ticks) {
        CompoundTag sequence = itemEntity.getPersistentData().getCompound(SEQUENCE_KEY);
        sequence.putInt(TICKS_KEY, ticks);
        itemEntity.getPersistentData().put(SEQUENCE_KEY, sequence);
    }

    private static CompoundTag getStackSequence(ItemEntity itemEntity) {
        CompoundTag stackTag = itemEntity.getItem().getTag();
        if (stackTag == null) {
            return new CompoundTag();
        }
        return stackTag.getCompound(SEQUENCE_KEY);
    }

    private static CompoundTag getOrCreateStackSequence(ItemEntity itemEntity) {
        return itemEntity.getItem().getOrCreateTag().getCompound(SEQUENCE_KEY);
    }

    private static void updateStackSequence(ItemEntity itemEntity, CompoundTag sequence) {
        ItemStack stack = itemEntity.getItem().copy();
        stack.getOrCreateTag().put(SEQUENCE_KEY, sequence);
        itemEntity.setItem(stack);
    }

    private static void clearSequence(ItemEntity itemEntity) {
        itemEntity.getPersistentData().remove(SEQUENCE_KEY);

        ItemStack stack = itemEntity.getItem().copy();
        CompoundTag tag = stack.getTag();
        if (tag != null) {
            tag.remove(SEQUENCE_KEY);
            if (tag.isEmpty()) {
                stack.setTag(null);
            }
            itemEntity.setItem(stack);
        }
    }
}

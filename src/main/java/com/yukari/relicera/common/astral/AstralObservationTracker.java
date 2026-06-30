package com.yukari.relicera.common.astral;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class AstralObservationTracker {
    private static final int REQUIRED_FOCUS_TICKS = 20;
    private static final double MOON_ALIGNMENT_THRESHOLD = 0.97D;
    private static final Map<UUID, FocusProgress> FOCUS_PROGRESS = new HashMap<>();

    private AstralObservationTracker() {
    }

    public static void tick(ServerPlayer player) {
        if (AstralObservationData.hasClaimedAstralLens(player)) {
            clearFocus(player);
            return;
        }

        if (!(player.level() instanceof ServerLevel level) || !isObservingWithSpyglass(player) || !canObserveMoon(player, level)) {
            clearFocus(player);
            return;
        }

        int moonPhase = level.getMoonPhase();
        if (hasAlreadyObserved(player, moonPhase)) {
            clearFocus(player);
            return;
        }

        UUID playerId = player.getUUID();
        FocusProgress progress = FOCUS_PROGRESS.compute(playerId, (id, current) -> {
            if (current == null || current.moonPhase() != moonPhase) {
                return new FocusProgress(moonPhase, 1);
            }
            return new FocusProgress(moonPhase, current.focusTicks() + 1);
        });

        if (progress.focusTicks() >= REQUIRED_FOCUS_TICKS && AstralObservationData.observeMoonPhase(player, moonPhase)) {
            FOCUS_PROGRESS.remove(playerId);
            playObservationSound(level, player);

            if (AstralObservationData.hasObservedAllMoonPhases(player)) {
                completeObservation(level, player);
            }
        }
    }

    public static void clearFocus(ServerPlayer player) {
        FOCUS_PROGRESS.remove(player.getUUID());
    }

    private static boolean isObservingWithSpyglass(ServerPlayer player) {
        return player.isUsingItem() && player.getUseItem().is(Items.SPYGLASS);
    }

    private static boolean canObserveMoon(ServerPlayer player, ServerLevel level) {
        if (level.dimension() != Level.OVERWORLD || !level.isNight()) {
            return false;
        }

        BlockPos eyePosition = BlockPos.containing(player.getEyePosition());
        if (!level.canSeeSky(eyePosition)) {
            return false;
        }

        Vec3 moonDirection = getMoonDirection(level);
        return moonDirection.y > 0.0D && player.getLookAngle().normalize().dot(moonDirection) >= MOON_ALIGNMENT_THRESHOLD;
    }

    private static boolean hasAlreadyObserved(ServerPlayer player, int moonPhase) {
        return (AstralObservationData.getObservedMoonPhaseMask(player) & (1 << (moonPhase & 7))) != 0;
    }

    private static Vec3 getMoonDirection(ServerLevel level) {
        double angle = level.getTimeOfDay(1.0F) * Math.PI * 2.0D;
        return new Vec3(Math.sin(angle), -Math.cos(angle), 0.0D).normalize();
    }

    private static void playObservationSound(ServerLevel level, ServerPlayer player) {
        level.playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                SoundEvents.EXPERIENCE_ORB_PICKUP,
                SoundSource.PLAYERS,
                0.1F,
                (level.random.nextFloat() - level.random.nextFloat()) * 0.35F + 0.9F
        );
    }

    private static void completeObservation(ServerLevel level, ServerPlayer player) {
        AstralObservationData.setClaimedAstralLens(player);
        breakSpyglassIfNeeded(player);
        AstralLensDropSequence.spawn(level, player);
    }

    private static void breakSpyglassIfNeeded(ServerPlayer player) {
        if (player.isCreative()) {
            return;
        }

        InteractionHand usedHand = player.getUsedItemHand();
        ItemStack spyglass = player.getItemInHand(usedHand);
        if (!spyglass.is(Items.SPYGLASS)) {
            return;
        }

        player.level().playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                SoundEvents.ITEM_BREAK,
                SoundSource.PLAYERS,
                0.8F,
                0.8F + player.getRandom().nextFloat() * 0.4F
        );
        spyglass.shrink(1);
        player.stopUsingItem();
    }

    private record FocusProgress(int moonPhase, int focusTicks) {
    }
}

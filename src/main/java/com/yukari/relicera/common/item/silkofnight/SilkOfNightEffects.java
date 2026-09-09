package com.yukari.relicera.common.item.silkofnight;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraftforge.event.VanillaGameEvent;

import java.util.Set;

public final class SilkOfNightEffects {
    private static final Set<GameEvent> MOVEMENT_GAME_EVENTS = Set.of(
            GameEvent.STEP,
            GameEvent.SWIM,
            GameEvent.SPLASH,
            GameEvent.FLAP,
            GameEvent.ELYTRA_GLIDE,
            GameEvent.HIT_GROUND
    );

    private SilkOfNightEffects() {
    }

    public static boolean shouldSuppressMovementSound(Player player, SoundEvent sound) {
        if (!SilkOfNightDecoration.isWearing(player)) {
            return false;
        }
        return sound == SoundEvents.PLAYER_SWIM
                || sound == SoundEvents.PLAYER_SPLASH
                || sound == SoundEvents.PLAYER_SPLASH_HIGH_SPEED
                || sound == SoundEvents.PLAYER_SMALL_FALL
                || sound == SoundEvents.PLAYER_BIG_FALL;
    }

    public static void suppressMovementVibrations(VanillaGameEvent event) {
        if (!(event.getCause() instanceof Player player) || !SilkOfNightDecoration.isWearing(player)) {
            return;
        }

        GameEvent gameEvent = event.getVanillaEvent();
        if (MOVEMENT_GAME_EVENTS.contains(gameEvent)
                || gameEvent == GameEvent.ENTITY_DAMAGE
                && player instanceof SilkOfNightFallContext fallContext
                && fallContext.relicera$isTakingFallDamage()) {
            event.setCanceled(true);
        }
    }
}

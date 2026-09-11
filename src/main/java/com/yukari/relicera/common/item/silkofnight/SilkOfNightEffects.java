package com.yukari.relicera.common.item.silkofnight;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraftforge.event.VanillaGameEvent;

public final class SilkOfNightEffects {
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
        if (gameEvent == GameEvent.HIT_GROUND
                || gameEvent == GameEvent.ENTITY_DAMAGE
                && player instanceof SilkOfNightFallContext fallContext
                && fallContext.relicera$isTakingFallDamage()) {
            event.setCanceled(true);
        }
    }
}

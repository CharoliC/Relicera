package com.yukari.relicera.common.effect.covenantbreaker;

import com.yukari.relicera.ReliceraMod;
import com.yukari.relicera.registry.ModEffects;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ReliceraMod.MOD_ID)
public final class CovenantBreakerEffects {
    private static final int BREAKER_DURATION = 2400 * 20;
    private static final int SLOWNESS_DURATION = 5 * 20;
    private static final String PENDING_DURATION_TAG = "ReliceraCovenantBreakerPendingDuration";

    private CovenantBreakerEffects() {
    }

    public static void apply(ServerPlayer player) {
        player.addEffect(new MobEffectInstance(ModEffects.COVENANT_BREAKER.get(), BREAKER_DURATION));
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, SLOWNESS_DURATION, 4));
    }

    @SubscribeEvent
    public static void onEffectRemove(MobEffectEvent.Remove event) {
        if (event.getEntity() instanceof ServerPlayer
                && event.getEffect() == ModEffects.COVENANT_BREAKER.get()) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (!(event.getEntity() instanceof ServerPlayer)) {
            return;
        }
        MobEffectInstance effect = event.getOriginal().getEffect(ModEffects.COVENANT_BREAKER.get());
        if (effect != null && effect.getDuration() > 0) {
            event.getEntity().getPersistentData().putInt(PENDING_DURATION_TAG, effect.getDuration());
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        CompoundTag data = player.getPersistentData();
        int duration = data.getInt(PENDING_DURATION_TAG);
        data.remove(PENDING_DURATION_TAG);
        if (duration > 0) {
            player.addEffect(new MobEffectInstance(ModEffects.COVENANT_BREAKER.get(), duration));
        }
    }
}

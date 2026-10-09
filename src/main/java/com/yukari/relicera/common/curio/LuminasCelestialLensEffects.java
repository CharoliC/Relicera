package com.yukari.relicera.common.curio;

import com.yukari.relicera.registry.ModItems;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.Event;
import top.theillusivec4.curios.api.CuriosApi;

public final class LuminasCelestialLensEffects {
    private static final String GRANTED_FLIGHT_KEY = "ReliceraLuminasCelestialLensGrantedFlight";

    private LuminasCelestialLensEffects() {
    }

    public static boolean isEquipped(LivingEntity entity) {
        return CuriosApi.getCuriosInventory(entity)
                .resolve()
                .map(handler -> handler.isEquipped(ModItems.LUMINAS_CELESTIAL_LENS.get()))
                .orElse(false);
    }

    public static void tickPlayerFlight(Player player) {
        if (player.level().isClientSide()) {
            return;
        }

        boolean equipped = isEquipped(player);
        if (equipped) {
            grantFlight(player);
            return;
        }

        revokeGrantedFlight(player);
    }

    public static void tickImmunities(LivingEntity entity) {
        if (entity.level().isClientSide() || !isEquipped(entity)) {
            return;
        }

        entity.removeEffect(MobEffects.DARKNESS);
        entity.removeEffect(MobEffects.BLINDNESS);
    }

    public static void preventDarknessAndBlindness(MobEffectEvent.Applicable event) {
        MobEffect effect = event.getEffectInstance().getEffect();
        if ((effect == MobEffects.DARKNESS || effect == MobEffects.BLINDNESS) && isEquipped(event.getEntity())) {
            event.setResult(Event.Result.DENY);
        }
    }

    private static void grantFlight(Player player) {
        if (player.getAbilities().mayfly) {
            return;
        }

        player.getPersistentData().putBoolean(GRANTED_FLIGHT_KEY, true);
        player.getAbilities().mayfly = true;
        player.onUpdateAbilities();
    }

    private static void revokeGrantedFlight(Player player) {
        if (!player.getPersistentData().getBoolean(GRANTED_FLIGHT_KEY)) {
            return;
        }

        player.getPersistentData().remove(GRANTED_FLIGHT_KEY);
        if (player.isCreative() || player.isSpectator()) {
            return;
        }

        player.getAbilities().mayfly = false;
        player.getAbilities().flying = false;
        player.onUpdateAbilities();
    }
}

package com.yukari.relicera.common.curio;

import com.yukari.relicera.config.ModCommonConfig;
import com.yukari.relicera.registry.ModItems;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraftforge.event.VanillaGameEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import top.theillusivec4.curios.api.CuriosApi;

public final class NightGlovesEffects {
    private NightGlovesEffects() {
    }

    public static boolean isEquipped(Player player) {
        return CuriosApi.getCuriosInventory(player)
                .resolve()
                .map(handler -> handler.isEquipped(ModItems.NIGHT_GLOVES.get()))
                .orElse(false);
    }

    public static void suppressContainerVibrations(VanillaGameEvent event) {
        if ((event.getVanillaEvent() == GameEvent.CONTAINER_OPEN || event.getVanillaEvent() == GameEvent.CONTAINER_CLOSE)
                && event.getCause() instanceof Player player
                && isEquipped(player)) {
            event.setCanceled(true);
        }
    }

    public static void applyNightMeleeDamageBonus(LivingHurtEvent event) {
        if (event.getSource().getEntity() instanceof Player player
                && event.getSource().getDirectEntity() == player
                && player.level().isNight()
                && isEquipped(player)) {
            event.setAmount(event.getAmount() * (1.0F + ModCommonConfig.NIGHT_GLOVES_NIGHT_ATTACK_DAMAGE_BONUS.get().floatValue()));
        }
    }
}

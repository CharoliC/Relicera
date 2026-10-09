package com.yukari.relicera.common.curio;

import com.yukari.relicera.registry.ModItems;
import net.minecraft.world.entity.player.Player;
import top.theillusivec4.curios.api.CuriosApi;

public final class RingOfSatietyEffects {
    private RingOfSatietyEffects() {
    }

    public static boolean isActive(Player player) {
        return !player.level().isClientSide() && CuriosApi.getCuriosInventory(player)
                .resolve()
                .map(handler -> handler.isEquipped(ModItems.RING_OF_SATIETY.get()))
                .orElse(false);
    }
}

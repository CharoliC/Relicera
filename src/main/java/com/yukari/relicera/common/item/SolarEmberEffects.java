package com.yukari.relicera.common.item;

import com.yukari.relicera.ReliceraMod;
import com.yukari.relicera.registry.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

public final class SolarEmberEffects {
    private static final String HELD_TICKS_TAG = ReliceraMod.MOD_ID + ".solar_ember_held_ticks";
    private static final int IGNITION_INTERVAL_TICKS = 100;
    private static final int FIRE_SECONDS = 4;

    private SolarEmberEffects() {
    }

    public static void tick(ServerPlayer player) {
        CompoundTag data = player.getPersistentData();
        if (!player.isAlive() || !isHoldingSolarEmber(player)) {
            data.remove(HELD_TICKS_TAG);
            return;
        }

        int heldTicks = data.getInt(HELD_TICKS_TAG) + 1;
        if (heldTicks >= IGNITION_INTERVAL_TICKS) {
            player.setSecondsOnFire(FIRE_SECONDS);
            heldTicks = 0;
        }

        data.putInt(HELD_TICKS_TAG, heldTicks);
    }

    private static boolean isHoldingSolarEmber(ServerPlayer player) {
        return player.getMainHandItem().is(ModItems.SOLAR_EMBER.get())
                || player.getOffhandItem().is(ModItems.SOLAR_EMBER.get());
    }
}

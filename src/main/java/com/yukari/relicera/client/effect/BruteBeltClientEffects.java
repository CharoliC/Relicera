package com.yukari.relicera.client.effect;

import com.yukari.relicera.mixin.LivingEntityAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

public final class BruteBeltClientEffects {
    private BruteBeltClientEffects() {
    }

    public static void syncAttackStrength(int attackStrengthTicker) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }

        LivingEntityAccessor accessor = (LivingEntityAccessor) player;
        accessor.relicera$setAttackStrengthTicker(Math.max(
                accessor.relicera$getAttackStrengthTicker(),
                attackStrengthTicker
        ));
    }
}

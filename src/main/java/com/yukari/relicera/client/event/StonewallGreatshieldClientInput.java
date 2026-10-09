package com.yukari.relicera.client.event;

import com.yukari.relicera.common.item.StonewallGreatshieldEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.common.ForgeHooks;

public final class StonewallGreatshieldClientInput {
    private StonewallGreatshieldClientInput() {
    }

    public static void handleAttackClicks(Minecraft minecraft) {
        LocalPlayer player = minecraft.player;
        if (player == null
                || minecraft.gameMode == null
                || minecraft.screen != null
                || !StonewallGreatshieldEffects.isUsingInMainHand(player)) {
            return;
        }

        while (minecraft.options.keyAttack.consumeClick()) {
            if (!StonewallGreatshieldEffects.canBash(player)
                    || player.getAttackStrengthScale(0.0F) < 1.0F) {
                continue;
            }

            var inputEvent = ForgeHooksClient.onClickInput(
                    0,
                    minecraft.options.keyAttack,
                    InteractionHand.MAIN_HAND
            );
            if (inputEvent.isCanceled()) {
                if (inputEvent.shouldSwingHand()) {
                    player.swing(InteractionHand.MAIN_HAND);
                }
                continue;
            }

            if (minecraft.hitResult instanceof EntityHitResult entityHitResult) {
                minecraft.gameMode.attack(player, entityHitResult.getEntity());
            } else {
                player.resetAttackStrengthTicker();
                ForgeHooks.onEmptyLeftClick(player);
            }

            if (inputEvent.shouldSwingHand()) {
                player.swing(InteractionHand.MAIN_HAND);
            }
        }
    }
}

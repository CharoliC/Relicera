package com.yukari.relicera.common.item;

import com.yukari.relicera.config.ModCommonConfig;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class TurncoatsMedalItem extends QuickEquipCurioItem {
    public TurncoatsMedalItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean overrideStackedOnOther(ItemStack carriedStack, Slot slot, ClickAction action, Player player) {
        if (action != ClickAction.SECONDARY
                || player.getAbilities().instabuild
                || !ItemStack.isSameItemSameTags(slot.getItem(), Raid.getLeaderBannerInstance())
                || !hasExtendableEffect(player)
                || ModCommonConfig.TURNCOATS_MEDAL_BANNER_EFFECT_EXTENSION_TICKS.get() <= 0) {
            return false;
        }

        if (player instanceof ServerPlayer serverPlayer) {
            int extensionTicks = ModCommonConfig.TURNCOATS_MEDAL_BANNER_EFFECT_EXTENSION_TICKS.get();
            extendEffect(serverPlayer, MobEffects.HERO_OF_THE_VILLAGE, extensionTicks);
            extendEffect(serverPlayer, MobEffects.BAD_OMEN, extensionTicks);
            slot.remove(1);
            serverPlayer.playNotifySound(SoundEvents.ILLUSIONER_CAST_SPELL, SoundSource.PLAYERS, 1.0F, 0.8F);
        }
        return true;
    }

    private static boolean hasExtendableEffect(Player player) {
        return player.hasEffect(MobEffects.HERO_OF_THE_VILLAGE) || player.hasEffect(MobEffects.BAD_OMEN);
    }

    private static void extendEffect(ServerPlayer player, MobEffect effect, int extensionTicks) {
        MobEffectInstance current = player.getEffect(effect);
        if (current == null) {
            return;
        }

        int duration = current.getDuration() > Integer.MAX_VALUE - extensionTicks
                ? Integer.MAX_VALUE
                : current.getDuration() + extensionTicks;
        player.addEffect(new MobEffectInstance(
                effect,
                duration,
                current.getAmplifier(),
                current.isAmbient(),
                current.isVisible(),
                current.showIcon()
        ));
    }
}

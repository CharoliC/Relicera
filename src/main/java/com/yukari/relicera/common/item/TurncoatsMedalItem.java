package com.yukari.relicera.common.item;

import com.yukari.relicera.common.network.ModNetworking;
import com.yukari.relicera.common.network.packet.UseTurncoatsMedalBannerPacket;
import com.yukari.relicera.config.ModCommonConfig;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.SlotAccess;
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
        if (!canConsumeBanner(slot.getItem(), action, player)) {
            return false;
        }

        handleBannerInteraction(player, () -> slot.remove(1));
        return true;
    }

    @Override
    public boolean overrideOtherStackedOnMe(
            ItemStack medalStack,
            ItemStack carriedStack,
            Slot slot,
            ClickAction action,
            Player player,
            SlotAccess carriedSlot
    ) {
        if (!canConsumeBanner(carriedStack, action, player)) {
            return false;
        }

        handleBannerInteraction(player, () -> carriedStack.shrink(1));
        return true;
    }

    private static boolean canConsumeBanner(ItemStack banner, ClickAction action, Player player) {
        return action == ClickAction.SECONDARY
                && ItemStack.isSameItemSameTags(banner, Raid.getLeaderBannerInstance())
                && hasExtendableEffect(player)
                && ModCommonConfig.TURNCOATS_MEDAL_BANNER_EFFECT_EXTENSION_TICKS.get() > 0;
    }

    private static void handleBannerInteraction(Player player, Runnable consumeBanner) {
        if (player.level().isClientSide()) {
            if (player.getAbilities().instabuild) {
                ModNetworking.sendToServer(new UseTurncoatsMedalBannerPacket());
            }
            return;
        }

        if (player instanceof ServerPlayer serverPlayer && !serverPlayer.getAbilities().instabuild) {
            extendEffects(serverPlayer);
            consumeBanner.run();
        }
    }

    public static void handleCreativeBannerInteraction(ServerPlayer player) {
        if (!player.getAbilities().instabuild
                || !hasExtendableEffect(player)
                || !hasCreativeInteractionTarget(player)
                || ModCommonConfig.TURNCOATS_MEDAL_BANNER_EFFECT_EXTENSION_TICKS.get() <= 0) {
            return;
        }
        extendEffects(player);
    }

    private static boolean hasCreativeInteractionTarget(ServerPlayer player) {
        ItemStack leaderBanner = Raid.getLeaderBannerInstance();
        return player.containerMenu.slots.stream()
                .map(Slot::getItem)
                .anyMatch(stack -> stack.getItem() instanceof TurncoatsMedalItem
                        || ItemStack.isSameItemSameTags(stack, leaderBanner));
    }

    private static boolean hasExtendableEffect(Player player) {
        return player.hasEffect(MobEffects.HERO_OF_THE_VILLAGE) || player.hasEffect(MobEffects.BAD_OMEN);
    }

    private static void extendEffects(ServerPlayer player) {
        int extensionTicks = ModCommonConfig.TURNCOATS_MEDAL_BANNER_EFFECT_EXTENSION_TICKS.get();
        extendEffect(player, MobEffects.HERO_OF_THE_VILLAGE, extensionTicks);
        extendEffect(player, MobEffects.BAD_OMEN, extensionTicks);
        player.playNotifySound(SoundEvents.ILLUSIONER_CAST_SPELL, SoundSource.PLAYERS, 1.0F, 0.8F);
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

package com.yukari.relicera.client.tooltip;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.List;

public final class HoldShiftTooltip {
    private HoldShiftTooltip() {
    }

    public static void append(
            List<Component> tooltip,
            Component flavor,
            Component holdShiftBefore,
            Component holdShiftKey,
            Component holdShiftAfter,
            List<Component> detailLines
    ) {
        tooltip.add(flavor.copy().withStyle(ChatFormatting.DARK_PURPLE));
        tooltip.add(Component.empty());

        if (Screen.hasShiftDown()) {
            detailLines.stream().map(Component::copy).forEach(tooltip::add);
            return;
        }

        tooltip.add(holdShiftBefore.copy()
                .withStyle(ChatFormatting.DARK_PURPLE)
                .append(holdShiftKey.copy().withStyle(ChatFormatting.GOLD))
                .append(holdShiftAfter.copy().withStyle(ChatFormatting.DARK_PURPLE)));
    }

    public static Component goldPrefixLine(String goldPrefix, String trailingTranslationKey) {
        MutableComponent line = Component.literal(goldPrefix).withStyle(ChatFormatting.GOLD);
        return line.append(Component.translatable(trailingTranslationKey).withStyle(ChatFormatting.DARK_PURPLE));
    }

    public static Component darkPurple(Component component) {
        return component.copy().withStyle(ChatFormatting.DARK_PURPLE);
    }

    public static Component lightPurple(Component component) {
        return component.copy().withStyle(ChatFormatting.LIGHT_PURPLE);
    }
}

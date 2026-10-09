package com.yukari.relicera.client.tooltip;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.Slot;

public final class ItemContentsCursorAnimation {
    private static final long DURATION_NANOS = 120_000_000L;
    private static Screen screen;
    private static Slot slot;
    private static ResourceLocation texture;
    private static int targetSlot;
    private static float startPosition;
    private static long startedAt;
    private static boolean rendered;

    private ItemContentsCursorAnimation() {
    }

    public static float getPosition(ResourceLocation frameTexture, int selectedSlot) {
        Screen currentScreen = Minecraft.getInstance().screen;
        Slot currentSlot = currentScreen instanceof AbstractContainerScreen<?> container
                ? container.getSlotUnderMouse() : null;
        if (currentSlot == null) {
            reset();
            return selectedSlot;
        }
        long now = System.nanoTime();
        if (screen != currentScreen || slot != currentSlot || !frameTexture.equals(texture)) {
            screen = currentScreen;
            slot = currentSlot;
            texture = frameTexture;
            snapTo(selectedSlot, now);
        } else if (targetSlot != selectedSlot) {
            snapTo(selectedSlot, now);
        }
        rendered = true;
        return sample(now);
    }

    public static void select(Screen currentScreen, Slot currentSlot, int previous, int selected, int capacity) {
        if (screen != currentScreen || slot != currentSlot) {
            return;
        }
        long now = System.nanoTime();
        if (Math.abs(selected - previous) == capacity - 1) {
            snapTo(selected, now);
        } else {
            startPosition = sample(now);
            targetSlot = selected;
            startedAt = now;
        }
    }

    public static void finishFrame(Screen renderedScreen) {
        if (screen != renderedScreen) {
            return;
        }
        if (!rendered) {
            reset();
        }
        rendered = false;
    }

    public static void close(Screen closedScreen) {
        if (screen == closedScreen) {
            reset();
        }
    }

    private static float sample(long now) {
        float progress = Math.min(1.0F, Math.max(0.0F, (float) (now - startedAt) / DURATION_NANOS));
        float remaining = 1.0F - progress;
        float eased = 1.0F - remaining * remaining * remaining;
        return startPosition + (targetSlot - startPosition) * eased;
    }

    private static void snapTo(int selected, long now) {
        startPosition = selected;
        targetSlot = selected;
        startedAt = now;
    }

    private static void reset() {
        screen = null;
        slot = null;
        texture = null;
        rendered = false;
    }
}

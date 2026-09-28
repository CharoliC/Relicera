package com.yukari.relicera.client.tooltip;

import com.mojang.blaze3d.systems.RenderSystem;
import com.yukari.relicera.ReliceraMod;
import java.util.List;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public final class ClientFiveSlotContentsTooltip implements ClientTooltipComponent {
    private static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath(
            ReliceraMod.MOD_ID, "textures/gui/tooltip_bar_5.png");
    private static final int WIDTH = 93;
    private static final int HEIGHT = 24;
    private static final int SLOT_COUNT = 5;
    private static final int SLOT_SPACING = 18;

    private final List<ItemStack> items;
    private final int selectedSlot;
    private final ResourceLocation frameTexture;

    public ClientFiveSlotContentsTooltip(List<ItemStack> items, int selectedSlot, ResourceLocation frameTexture) {
        this.items = items;
        this.selectedSlot = selectedSlot;
        this.frameTexture = frameTexture;
    }

    @Override
    public int getHeight() {
        return HEIGHT + 4;
    }

    @Override
    public int getWidth(Font font) {
        return WIDTH;
    }

    @Override
    public void renderImage(Font font, int x, int y, GuiGraphics graphics) {
        graphics.flush();
        graphics.pose().pushPose();
        try {
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            graphics.blit(BACKGROUND, x, y, 0, 0, WIDTH, HEIGHT, 128, 32);
            for (int slot = 0; slot < Math.min(SLOT_COUNT, items.size()); slot++) {
                ItemStack stack = items.get(slot);
                int slotX = x + 3 + slot * SLOT_SPACING;
                graphics.renderItem(stack, slotX, y + 5, slot);
                graphics.renderItemDecorations(font, stack, slotX, y + 5);
            }
            graphics.flush();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            graphics.pose().translate(0.0F, 0.0F, 250.0F);
            graphics.blit(frameTexture, x, y, 0, 0, WIDTH, HEIGHT, 128, 32);
            graphics.pose().translate(0.0F, 0.0F, 1.0F);
            if (selectedSlot >= 0 && selectedSlot < SLOT_COUNT) {
                float cursorPosition = ItemContentsCursorAnimation.getPosition(frameTexture, selectedSlot);
                graphics.pose().translate(cursorPosition * SLOT_SPACING, 0.0F, 0.0F);
                graphics.blit(frameTexture, x + 3, y + 5, 97, 5, 16, 17, 128, 32);
            }
        } finally {
            graphics.pose().popPose();
            RenderSystem.disableBlend();
        }
    }
}

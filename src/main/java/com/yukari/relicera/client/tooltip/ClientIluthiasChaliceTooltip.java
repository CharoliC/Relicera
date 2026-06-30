package com.yukari.relicera.client.tooltip;

import com.yukari.relicera.common.tooltip.IluthiasChaliceTooltip;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class ClientIluthiasChaliceTooltip implements ClientTooltipComponent {
    private static final ResourceLocation TEXTURE_LOCATION = ResourceLocation.withDefaultNamespace("textures/gui/container/bundle.png");
    private static final int SLOT_WIDTH = 18;
    private static final int SLOT_HEIGHT = 20;
    private static final int BORDER_WIDTH = 1;
    private static final int MARGIN_Y = 4;
    private static final int TEXTURE_SIZE = 128;

    private final NonNullList<ItemStack> totems;
    private final int capacity;

    public ClientIluthiasChaliceTooltip(IluthiasChaliceTooltip tooltip) {
        this.totems = tooltip.totems();
        this.capacity = tooltip.capacity();
    }

    @Override
    public int getHeight() {
        return SLOT_HEIGHT + BORDER_WIDTH * 2 + MARGIN_Y;
    }

    @Override
    public int getWidth(Font font) {
        return capacity * SLOT_WIDTH + BORDER_WIDTH * 2;
    }

    @Override
    public void renderImage(Font font, int x, int y, GuiGraphics graphics) {
        int slotY = y + BORDER_WIDTH;
        for (int slot = 0; slot < capacity; slot++) {
            int slotX = x + BORDER_WIDTH + slot * SLOT_WIDTH;
            blit(graphics, slotX, slotY, Texture.SLOT);
            if (slot < totems.size()) {
                ItemStack stack = totems.get(slot);
                graphics.renderItem(stack, slotX + 1, slotY + 1, slot);
                graphics.renderItemDecorations(font, stack, slotX + 1, slotY + 1);
                if (slot == 0) {
                    AbstractContainerScreen.renderSlotHighlight(graphics, slotX + 1, slotY + 1, 0);
                }
            }
        }
        drawBorder(x, y, graphics);
    }

    private void drawBorder(int x, int y, GuiGraphics graphics) {
        blit(graphics, x, y, Texture.BORDER_CORNER_TOP);
        blit(graphics, x + capacity * SLOT_WIDTH + BORDER_WIDTH, y, Texture.BORDER_CORNER_TOP);
        blit(graphics, x, y + SLOT_HEIGHT + BORDER_WIDTH, Texture.BORDER_CORNER_BOTTOM);
        blit(graphics, x + capacity * SLOT_WIDTH + BORDER_WIDTH, y + SLOT_HEIGHT + BORDER_WIDTH, Texture.BORDER_CORNER_BOTTOM);

        for (int slot = 0; slot < capacity; slot++) {
            blit(graphics, x + BORDER_WIDTH + slot * SLOT_WIDTH, y, Texture.BORDER_HORIZONTAL_TOP);
            blit(graphics, x + BORDER_WIDTH + slot * SLOT_WIDTH, y + SLOT_HEIGHT + BORDER_WIDTH, Texture.BORDER_HORIZONTAL_BOTTOM);
        }

        blit(graphics, x, y + BORDER_WIDTH, Texture.BORDER_VERTICAL);
        blit(graphics, x + capacity * SLOT_WIDTH + BORDER_WIDTH, y + BORDER_WIDTH, Texture.BORDER_VERTICAL);
    }

    private void blit(GuiGraphics graphics, int x, int y, Texture texture) {
        graphics.blit(TEXTURE_LOCATION, x, y, 0, texture.x, texture.y, texture.width, texture.height, TEXTURE_SIZE, TEXTURE_SIZE);
    }

    private enum Texture {
        SLOT(0, 0, 18, 20),
        BORDER_VERTICAL(0, 18, 1, 20),
        BORDER_HORIZONTAL_TOP(0, 20, 18, 1),
        BORDER_HORIZONTAL_BOTTOM(0, 60, 18, 1),
        BORDER_CORNER_TOP(0, 20, 1, 1),
        BORDER_CORNER_BOTTOM(0, 60, 1, 1);

        private final int x;
        private final int y;
        private final int width;
        private final int height;

        Texture(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
    }
}

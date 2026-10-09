package com.yukari.relicera.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.yukari.relicera.ReliceraMod;
import com.yukari.relicera.common.menu.FourfoldSherdPendantMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class FourfoldSherdPendantScreen extends AbstractContainerScreen<FourfoldSherdPendantMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(ReliceraMod.MOD_ID, "textures/gui/sherd_pendant.png");
    private static final int TITLE_COLOR = 0x404040;

    public FourfoldSherdPendantScreen(FourfoldSherdPendantMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShaderTexture(0, TEXTURE);
        guiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, Component.translatable("container.relicera.fourfold_sherd_pendant.title"), 8, 6, TITLE_COLOR, false);
    }
}

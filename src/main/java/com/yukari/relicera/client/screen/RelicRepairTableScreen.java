package com.yukari.relicera.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.yukari.relicera.ReliceraMod;
import com.yukari.relicera.common.block.RelicRepairTableBlockEntity;
import com.yukari.relicera.common.menu.RelicRepairTableMenu;
import com.yukari.relicera.common.recipe.RelicRepairRecipe;
import com.yukari.relicera.common.recipe.RelicRepairRecipes;
import java.util.List;
import net.minecraft.util.Mth;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class RelicRepairTableScreen extends AbstractContainerScreen<RelicRepairTableMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(ReliceraMod.MOD_ID, "textures/gui/relic_repair_table.png");
    private static final int ARROW_X = 82;
    private static final int ARROW_Y = 32;
    private static final int ARROW_U = 0;
    private static final int ARROW_V = 166;
    private static final int ARROW_WIDTH = 13;
    private static final int ARROW_HEIGHT = 14;
    private static final int GHOST_RELIC_SLOT_X = 79;
    private static final int GHOST_RELIC_SLOT_Y = 11;
    private static final int GHOST_RELIC_SLOT_U_START = 0;
    private static final int GHOST_RELIC_SLOT_V = 180;
    private static final int GHOST_RELIC_SLOT_SIZE = 18;
    private static final int GHOST_RELIC_SLOT_FRAMES = 6;
    private static final int GHOST_RELIC_FRAME_TICKS = 40;
    private static final int GHOST_RELIC_FADE_TICKS = 14;
    private static final int MATERIAL_SLOT_FRAME_SIZE = 18;
    private static final int[] MATERIAL_SLOT_FRAME_X = {105, 44, 114};
    private static final int[] MATERIAL_SLOT_FRAME_Y = {20, 44, 44};
    private static final int TITLE_COLOR = 0x404040;

    public RelicRepairTableScreen(RelicRepairTableMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderMaterialHintTooltip(guiGraphics, mouseX, mouseY);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShaderTexture(0, TEXTURE);
        int x = this.leftPos;
        int y = this.topPos;
        guiGraphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);
        renderEmptyRelicSlotGhost(guiGraphics, x, y, partialTick);
        renderRepairProgress(guiGraphics, x, y);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, Component.translatable("container.relicera.relic_repair_table.title"), 8, 6, TITLE_COLOR, false);
    }

    private void renderRepairProgress(GuiGraphics guiGraphics, int x, int y) {
        int height = this.menu.getScaledRepairProgress(ARROW_HEIGHT);
        if (height > 0) {
            guiGraphics.blit(TEXTURE, x + ARROW_X, y + ARROW_Y, ARROW_U, ARROW_V, ARROW_WIDTH, height);
        }
    }

    private void renderEmptyRelicSlotGhost(GuiGraphics guiGraphics, int x, int y, float partialTick) {
        if (!this.menu.getRepairSlotItem(RelicRepairTableBlockEntity.SLOT_RELIC).isEmpty() || this.minecraft == null || this.minecraft.level == null) {
            return;
        }

        float time = this.minecraft.level.getGameTime() + partialTick;
        int frame = Mth.floor(time / GHOST_RELIC_FRAME_TICKS) % GHOST_RELIC_SLOT_FRAMES;
        float frameTime = time % GHOST_RELIC_FRAME_TICKS;
        float fadeProgress = Mth.clamp((frameTime - (GHOST_RELIC_FRAME_TICKS - GHOST_RELIC_FADE_TICKS)) / GHOST_RELIC_FADE_TICKS, 0.0F, 1.0F);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        blitGhostRelicFrame(guiGraphics, x, y, frame, 1.0F - fadeProgress);
        if (fadeProgress > 0.0F) {
            blitGhostRelicFrame(guiGraphics, x, y, (frame + 1) % GHOST_RELIC_SLOT_FRAMES, fadeProgress);
        }
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();
    }

    private void blitGhostRelicFrame(GuiGraphics guiGraphics, int x, int y, int frame, float alpha) {
        if (alpha <= 0.0F) {
            return;
        }

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
        guiGraphics.blit(
                TEXTURE,
                x + GHOST_RELIC_SLOT_X,
                y + GHOST_RELIC_SLOT_Y,
                GHOST_RELIC_SLOT_U_START + frame * GHOST_RELIC_SLOT_SIZE,
                GHOST_RELIC_SLOT_V,
                GHOST_RELIC_SLOT_SIZE,
                GHOST_RELIC_SLOT_SIZE
        );
    }

    private void renderMaterialHintTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        getCurrentRecipe().ifPresent(recipe -> {
            for (int index = 0; index < RelicRepairRecipe.MATERIAL_COUNT; index++) {
                if (this.menu.getRepairSlotItem(RelicRepairTableBlockEntity.SLOT_MATERIAL_1 + index).isEmpty()
                        && isHoveringMaterialSlot(index, mouseX, mouseY)) {
                    guiGraphics.renderComponentTooltip(
                            this.font,
                            List.of(recipe.hint(index).copy().withStyle(ChatFormatting.GRAY)),
                            mouseX,
                            mouseY
                    );
                    return;
                }
            }
        });
    }

    private boolean isHoveringMaterialSlot(int index, int mouseX, int mouseY) {
        int x = this.leftPos + MATERIAL_SLOT_FRAME_X[index];
        int y = this.topPos + MATERIAL_SLOT_FRAME_Y[index];
        return mouseX >= x
                && mouseX < x + MATERIAL_SLOT_FRAME_SIZE
                && mouseY >= y
                && mouseY < y + MATERIAL_SLOT_FRAME_SIZE;
    }

    private java.util.Optional<RelicRepairRecipe> getCurrentRecipe() {
        return RelicRepairRecipes.findForRelic(this.menu.getRepairSlotItem(RelicRepairTableBlockEntity.SLOT_RELIC));
    }
}

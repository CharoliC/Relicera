package com.yukari.relicera.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.yukari.relicera.common.block.RelicRepairTableBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class RelicRepairTableRenderer implements BlockEntityRenderer<RelicRepairTableBlockEntity> {
    private static final float DISPLAY_Y_OFFSET = 1.15F;
    private static final float DISPLAY_SCALE = 1.0F;
    private static final float BOB_HEIGHT = 0.06F;
    private static final float BOB_SPEED = 0.12F;
    private static final float ROTATION_SPEED = 2.0F;

    private final ItemRenderer itemRenderer;

    public RelicRepairTableRenderer(BlockEntityRendererProvider.Context context) {
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(RelicRepairTableBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        ItemStack displayStack = getDisplayStack(blockEntity);
        Level level = blockEntity.getLevel();
        if (displayStack.isEmpty() || level == null) {
            return;
        }

        float time = level.getGameTime() + partialTick;
        float bob = Mth.sin(time * BOB_SPEED) * BOB_HEIGHT;
        float rotation = (time * ROTATION_SPEED) % 360.0F;

        poseStack.pushPose();
        poseStack.translate(0.5D, DISPLAY_Y_OFFSET + bob, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(rotation));
        poseStack.scale(DISPLAY_SCALE, DISPLAY_SCALE, DISPLAY_SCALE);
        itemRenderer.renderStatic(displayStack, ItemDisplayContext.GROUND, packedLight, OverlayTexture.NO_OVERLAY, poseStack, bufferSource, level, 0);
        poseStack.popPose();
    }

    private static ItemStack getDisplayStack(RelicRepairTableBlockEntity blockEntity) {
        ItemStack output = blockEntity.getItemHandler().getStackInSlot(RelicRepairTableBlockEntity.SLOT_OUTPUT);
        if (!output.isEmpty()) {
            return output;
        }
        return blockEntity.getItemHandler().getStackInSlot(RelicRepairTableBlockEntity.SLOT_RELIC);
    }
}

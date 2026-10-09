package com.yukari.relicera.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.yukari.relicera.ReliceraMod;
import com.yukari.relicera.client.model.BruteBeltModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

public final class BruteBeltRenderer implements ICurioRenderer.HumanoidRender {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            ReliceraMod.MOD_ID, "textures/entity/curio/brute_belt.png");

    private final BruteBeltModel model = new BruteBeltModel(
            Minecraft.getInstance().getEntityModels().bakeLayer(BruteBeltModel.LAYER_LOCATION));

    @Override
    public HumanoidModel<LivingEntity> getModel(ItemStack stack, SlotContext slotContext) {
        return model;
    }

    @Override
    public ResourceLocation getModelTexture(ItemStack stack, SlotContext slotContext) {
        return TEXTURE;
    }

    @Override
    public void renderModel(
            ItemStack stack,
            SlotContext slotContext,
            PoseStack poseStack,
            RenderLayerParent<LivingEntity, EntityModel<LivingEntity>> renderLayerParent,
            MultiBufferSource buffers,
            int packedLight) {
        if (renderLayerParent.getModel() instanceof HumanoidModel<?> humanoidModel) {
            model.body.copyFrom(humanoidModel.body);
        }
        model.body.visible = true;
        model.body.render(
                poseStack,
                ItemRenderer.getFoilBuffer(
                        buffers,
                        model.renderType(TEXTURE),
                        false,
                        stack.hasFoil()),
                packedLight,
                OverlayTexture.NO_OVERLAY
        );
    }
}

package com.yukari.relicera.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.yukari.relicera.ReliceraMod;
import com.yukari.relicera.client.model.SurfaceGlovesModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

public final class SurfaceGlovesRenderer implements ICurioRenderer.HumanoidRender, FirstPersonGloveRenderer {
    private final ModelVariant wideVariant;
    private final ModelVariant slimVariant;

    private SurfaceGlovesRenderer(
            ModelLayerLocation wideLayer,
            ResourceLocation wideTexture,
            ResourceLocation wideGlowTexture,
            ModelLayerLocation slimLayer,
            ResourceLocation slimTexture,
            ResourceLocation slimGlowTexture) {
        wideVariant = createVariant(wideLayer, wideTexture, wideGlowTexture);
        slimVariant = createVariant(slimLayer, slimTexture, slimGlowTexture);
    }

    public static SurfaceGlovesRenderer leatherGloves() {
        return new SurfaceGlovesRenderer(
                SurfaceGlovesModel.LEATHER_WIDE_LAYER_LOCATION,
                texture("leather_gloves.png"),
                null,
                SurfaceGlovesModel.LEATHER_SLIM_LAYER_LOCATION,
                texture("leather_gloves_slim.png"),
                null);
    }

    public static SurfaceGlovesRenderer nightGloves() {
        return new SurfaceGlovesRenderer(
                SurfaceGlovesModel.NIGHT_WIDE_LAYER_LOCATION,
                texture("night_gloves.png"),
                texture("night_gloves_glow.png"),
                SurfaceGlovesModel.NIGHT_SLIM_LAYER_LOCATION,
                texture("night_gloves_slim.png"),
                texture("night_gloves_slim_glow.png"));
    }

    public static SurfaceGlovesRenderer thousandweightGauntlets() {
        return new SurfaceGlovesRenderer(
                SurfaceGlovesModel.THOUSANDWEIGHT_WIDE_LAYER_LOCATION,
                texture("thousandweight_gauntlets.png"),
                texture("thousandweight_gauntlets_glow.png"),
                SurfaceGlovesModel.THOUSANDWEIGHT_SLIM_LAYER_LOCATION,
                texture("thousandweight_gauntlets_slim.png"),
                texture("thousandweight_gauntlets_slim_glow.png"));
    }

    @Override
    public HumanoidModel<LivingEntity> getModel(ItemStack stack, SlotContext slotContext) {
        return getVariant(slotContext.entity()).model();
    }

    @Override
    public ResourceLocation getModelTexture(ItemStack stack, SlotContext slotContext) {
        return getVariant(slotContext.entity()).texture();
    }

    @Override
    public void renderModel(
            ItemStack stack,
            SlotContext slotContext,
            PoseStack poseStack,
            RenderLayerParent<LivingEntity, EntityModel<LivingEntity>> renderLayerParent,
            MultiBufferSource buffers,
            int packedLight) {
        ModelVariant variant = getVariant(slotContext.entity());
        copyArmPoses(variant.model(), renderLayerParent.getModel());
        renderArm(variant, stack, HumanoidArm.LEFT, poseStack, buffers, packedLight);
        renderArm(variant, stack, HumanoidArm.RIGHT, poseStack, buffers, packedLight);
    }

    @Override
    public void renderFirstPersonArm(
            AbstractClientPlayer player,
            ItemStack stack,
            HumanoidArm arm,
            ModelPart sourceArm,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int packedLight) {
        ModelVariant variant = getVariant(player);
        ModelPart armPart = variant.model().arm(arm);
        armPart.copyFrom(sourceArm);
        armPart.visible = true;
        renderArm(variant, stack, arm, poseStack, buffers, packedLight);
    }

    private void renderArm(
            ModelVariant variant,
            ItemStack stack,
            HumanoidArm arm,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int packedLight) {
        SurfaceGlovesModel model = variant.model();
        poseStack.pushPose();
        model.arm(arm).translateAndRotate(poseStack);
        model.glove(arm).render(
                poseStack,
                ItemRenderer.getFoilBuffer(
                        buffers,
                        model.renderType(variant.texture()),
                        false,
                        stack.hasFoil()),
                packedLight,
                OverlayTexture.NO_OVERLAY);
        if (variant.glowTexture() != null) {
            model.glove(arm).render(
                    poseStack,
                    ItemRenderer.getFoilBuffer(
                            buffers,
                            model.renderType(variant.glowTexture()),
                            false,
                            stack.hasFoil()),
                    LightTexture.pack(15, 15),
                    OverlayTexture.NO_OVERLAY);
        }
        poseStack.popPose();
    }

    private ModelVariant getVariant(LivingEntity entity) {
        return entity instanceof AbstractClientPlayer player && "slim".equals(player.getModelName())
                ? slimVariant
                : wideVariant;
    }

    private static void copyArmPoses(
            HumanoidModel<LivingEntity> model,
            EntityModel<LivingEntity> wearerModel) {
        if (wearerModel instanceof HumanoidModel<?> humanoidModel) {
            model.leftArm.copyFrom(humanoidModel.leftArm);
            model.rightArm.copyFrom(humanoidModel.rightArm);
        }
    }

    private static ModelVariant createVariant(
            ModelLayerLocation layer,
            ResourceLocation texture,
            ResourceLocation glowTexture) {
        return new ModelVariant(
                new SurfaceGlovesModel(Minecraft.getInstance().getEntityModels().bakeLayer(layer)),
                texture,
                glowTexture);
    }

    private static ResourceLocation texture(String fileName) {
        return ResourceLocation.fromNamespaceAndPath(
                ReliceraMod.MOD_ID, "textures/entity/curio/" + fileName);
    }

    private record ModelVariant(
            SurfaceGlovesModel model,
            ResourceLocation texture,
            ResourceLocation glowTexture) {
    }
}

package com.yukari.relicera.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.yukari.relicera.ReliceraMod;
import com.yukari.relicera.client.model.TreasureHuntersGlovesModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

public final class TreasureHuntersGlovesRenderer
        implements ICurioRenderer.HumanoidRender, FirstPersonGloveRenderer {
    private static final ResourceLocation WIDE_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            ReliceraMod.MOD_ID, "textures/entity/curio/treasure_hunters_gloves.png");
    private static final ResourceLocation WIDE_GLOW_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            ReliceraMod.MOD_ID, "textures/entity/curio/treasure_hunters_gloves_glow.png");
    private static final ResourceLocation SLIM_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            ReliceraMod.MOD_ID, "textures/entity/curio/treasure_hunters_gloves_slim.png");
    private static final ResourceLocation SLIM_GLOW_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            ReliceraMod.MOD_ID, "textures/entity/curio/treasure_hunters_gloves_slim_glow.png");

    private final ModelVariant wideVariant;
    private final ModelVariant slimVariant;

    public TreasureHuntersGlovesRenderer() {
        wideVariant = new ModelVariant(new TreasureHuntersGlovesModel(
                Minecraft.getInstance().getEntityModels().bakeLayer(
                        TreasureHuntersGlovesModel.WIDE_LAYER_LOCATION)),
                WIDE_TEXTURE,
                WIDE_GLOW_TEXTURE);
        slimVariant = new ModelVariant(new TreasureHuntersGlovesModel(
                Minecraft.getInstance().getEntityModels().bakeLayer(
                        TreasureHuntersGlovesModel.SLIM_LAYER_LOCATION)),
                SLIM_TEXTURE,
                SLIM_GLOW_TEXTURE);
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
        TreasureHuntersGlovesModel model = variant.model();
        poseStack.pushPose();
        model.arm(arm).translateAndRotate(poseStack);
        model.glove(arm).render(
                poseStack,
                ItemRenderer.getArmorFoilBuffer(
                        buffers,
                        RenderType.armorCutoutNoCull(variant.texture()),
                        false,
                        stack.hasFoil()),
                packedLight,
                OverlayTexture.NO_OVERLAY);
        model.outline(arm).render(
                poseStack,
                buffers.getBuffer(ReliceraRenderTypes.invertedGlow(variant.glowTexture())),
                LightTexture.FULL_BRIGHT,
                OverlayTexture.NO_OVERLAY);
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

    private record ModelVariant(
            TreasureHuntersGlovesModel model,
            ResourceLocation texture,
            ResourceLocation glowTexture) {
    }
}

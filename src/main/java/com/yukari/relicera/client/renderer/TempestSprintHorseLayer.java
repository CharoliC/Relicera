package com.yukari.relicera.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.yukari.relicera.ReliceraMod;
import com.yukari.relicera.common.effect.TempestSprintEffects;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HorseModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.horse.Horse;

public class TempestSprintHorseLayer extends RenderLayer<Horse, HorseModel<Horse>> {
    private static final float LIGHTNING_LAYER_SCALE = 1.1F;
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            ReliceraMod.MOD_ID,
            "textures/entity/horse/tempest_sprint_layer.png"
    );

    private final HorseModel<Horse> model;

    public TempestSprintHorseLayer(RenderLayerParent<Horse, HorseModel<Horse>> parent, EntityModelSet modelSet) {
        super(parent);
        this.model = new HorseModel<>(modelSet.bakeLayer(ModelLayers.HORSE));
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, Horse horse,
                       float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
                       float netHeadYaw, float headPitch) {
        if (!TempestSprintEffects.hasTempestSprintVisual(horse)) {
            return;
        }

        float tick = horse.tickCount + partialTick;
        EntityModel<Horse> parentModel = this.getParentModel();
        this.model.prepareMobModel(horse, limbSwing, limbSwingAmount, partialTick);
        parentModel.copyPropertiesTo(this.model);
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.energySwirl(
                TEXTURE,
                tick * 0.01F % 1.0F,
                tick * 0.01F % 1.0F
        ));
        this.model.setupAnim(horse, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        poseStack.pushPose();
        poseStack.scale(LIGHTNING_LAYER_SCALE, LIGHTNING_LAYER_SCALE, LIGHTNING_LAYER_SCALE);
        this.model.renderToBuffer(poseStack, vertexConsumer, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY,
                0.5F, 0.5F, 0.5F, 1.0F);
        poseStack.popPose();
    }
}

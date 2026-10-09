package com.yukari.relicera.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.yukari.relicera.ReliceraMod;
import com.yukari.relicera.client.model.BeltModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

public final class BeltRenderer implements ICurioRenderer.HumanoidRender {
    private final BeltModel model;
    private final ResourceLocation texture;

    private BeltRenderer(ModelLayerLocation layer, String textureName) {
        model = new BeltModel(Minecraft.getInstance().getEntityModels().bakeLayer(layer));
        texture = ResourceLocation.fromNamespaceAndPath(
                ReliceraMod.MOD_ID, "textures/entity/curio/" + textureName + ".png");
    }

    public static BeltRenderer warriorBelt() {
        return new BeltRenderer(BeltModel.WARRIOR_LAYER_LOCATION, "warrior_belt");
    }

    public static BeltRenderer littleTailorsBelt() {
        return new BeltRenderer(BeltModel.LITTLE_TAILORS_LAYER_LOCATION, "little_tailors_belt");
    }

    public static BeltRenderer verdantScaleBelt() {
        return new BeltRenderer(BeltModel.VERDANT_SCALE_LAYER_LOCATION, "verdant_scale_belt");
    }

    @Override
    public HumanoidModel<LivingEntity> getModel(ItemStack stack, SlotContext slotContext) {
        return model;
    }

    @Override
    public ResourceLocation getModelTexture(ItemStack stack, SlotContext slotContext) {
        return texture;
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
                ItemRenderer.getFoilBuffer(buffers, model.renderType(texture), false, stack.hasFoil()),
                packedLight,
                OverlayTexture.NO_OVERLAY
        );
    }
}

package com.yukari.relicera.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;

public interface FirstPersonGloveRenderer {
    void renderFirstPersonArm(
            AbstractClientPlayer player,
            ItemStack stack,
            HumanoidArm arm,
            ModelPart sourceArm,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int packedLight);
}

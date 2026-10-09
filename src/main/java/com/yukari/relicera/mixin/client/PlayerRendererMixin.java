package com.yukari.relicera.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.yukari.relicera.client.renderer.GlovesFirstPersonRenderer;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.entity.HumanoidArm;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerRenderer.class)
public abstract class PlayerRendererMixin {
    @Inject(method = "renderHand", at = @At("TAIL"))
    private void relicera$renderEquippedGloves(
            PoseStack poseStack,
            MultiBufferSource buffers,
            int packedLight,
            AbstractClientPlayer player,
            ModelPart armPart,
            ModelPart sleevePart,
            CallbackInfo ci
    ) {
        PlayerModel<AbstractClientPlayer> playerModel = ((PlayerRenderer) (Object) this).getModel();
        HumanoidArm arm = armPart == playerModel.leftArm ? HumanoidArm.LEFT : HumanoidArm.RIGHT;
        GlovesFirstPersonRenderer.renderEquippedArm(
                player, arm, armPart, poseStack, buffers, packedLight);
    }
}

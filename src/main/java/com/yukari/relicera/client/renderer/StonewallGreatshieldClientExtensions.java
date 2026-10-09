package com.yukari.relicera.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.yukari.relicera.common.item.StonewallGreatshieldEffects;
import com.yukari.relicera.registry.ModItems;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.Nullable;

public final class StonewallGreatshieldClientExtensions implements IClientItemExtensions {
    public static final StonewallGreatshieldClientExtensions INSTANCE = new StonewallGreatshieldClientExtensions();

    private static final HumanoidModel.ArmPose BASH_POSE = HumanoidModel.ArmPose.create(
            "RELICERA_STONEWALL_GREATSHIELD_BASH",
            false,
            StonewallGreatshieldClientExtensions::applyBashPose
    );

    private StonewallGreatshieldClientExtensions() {
    }

    @Override
    public @Nullable HumanoidModel.ArmPose getArmPose(
            LivingEntity entity,
            InteractionHand hand,
            ItemStack stack
    ) {
        if (hand != InteractionHand.MAIN_HAND
                || !stack.is(ModItems.STONEWALL_GREATSHIELD.get())
                || !StonewallGreatshieldEffects.isUsingInMainHand(entity)) {
            return null;
        }

        return entity.swingTime > 0 ? BASH_POSE : HumanoidModel.ArmPose.BLOCK;
    }

    @Override
    public boolean applyForgeHandTransform(
            PoseStack poseStack,
            LocalPlayer player,
            HumanoidArm arm,
            ItemStack stack,
            float partialTick,
            float equipProcess,
            float swingProcess
    ) {
        if (!stack.is(ModItems.STONEWALL_GREATSHIELD.get())
                || !StonewallGreatshieldEffects.isUsingInMainHand(player)) {
            return false;
        }

        float thrust = Mth.sin(Mth.clamp(swingProcess, 0.0F, 1.0F) * Mth.PI);
        if (thrust <= 0.0F) {
            return false;
        }

        float side = arm == HumanoidArm.RIGHT ? 1.0F : -1.0F;
        poseStack.translate(side * -0.035F * thrust, 0.06F * thrust, -0.42F * thrust);
        poseStack.mulPose(Axis.XP.rotationDegrees(-16.0F * thrust));
        poseStack.mulPose(Axis.ZP.rotationDegrees(side * -4.0F * thrust));
        return false;
    }

    private static void applyBashPose(HumanoidModel<?> model, LivingEntity entity, HumanoidArm arm) {
        ModelPart armPart = arm == HumanoidArm.RIGHT ? model.rightArm : model.leftArm;
        float thrust = Mth.sin(Mth.clamp(model.attackTime, 0.0F, 1.0F) * Mth.PI);
        float guardYaw = arm == HumanoidArm.RIGHT ? -Mth.PI / 6.0F : Mth.PI / 6.0F;

        armPart.xRot = Mth.lerp(thrust, -0.9424779F, -Mth.HALF_PI);
        armPart.yRot = Mth.lerp(thrust, guardYaw, 0.0F);
        armPart.zRot = 0.0F;
        armPart.z -= 3.0F * thrust;
    }
}

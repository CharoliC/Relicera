package com.yukari.relicera.mixin.client;

import com.yukari.relicera.common.curio.LittleTailorsBeltClientData;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin {
    @Inject(method = "isShaking", at = @At("RETURN"), cancellable = true)
    private void relicera$shakeIntimidatedHumanoid(
            LivingEntity entity,
            CallbackInfoReturnable<Boolean> callback
    ) {
        if (!callback.getReturnValue() && LittleTailorsBeltClientData.shouldShake(entity)) {
            callback.setReturnValue(true);
        }
    }
}

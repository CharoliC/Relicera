package com.yukari.relicera.mixin;

import com.yukari.relicera.common.item.TempestsReinsEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Inject(method = "isInWater", at = @At("HEAD"), cancellable = true)
    private void relicera$ignoreWaterStateForTempestsReinsWaterWalking(CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof LivingEntity livingEntity && TempestsReinsEffects.shouldIgnoreWaterState(livingEntity)) {
            cir.setReturnValue(false);
        }
    }
}

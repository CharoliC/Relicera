package com.yukari.relicera.mixin;

import com.yukari.relicera.common.curio.GranbellsFurnaceEffects;
import com.yukari.relicera.common.curio.IluthiasChaliceEffects;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Inject(method = "checkTotemDeathProtection", at = @At("HEAD"), cancellable = true)
    private void relicera$useEnhancedTotemWithIluthiasChalice(DamageSource source, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (IluthiasChaliceEffects.tryUseEnhancedTotem(self, source)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "canStandOnFluid", at = @At("RETURN"), cancellable = true)
    private void relicera$canStandOnLavaWithGranbellsFurnace(FluidState fluidState, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue()) {
            return;
        }

        LivingEntity self = (LivingEntity) (Object) this;
        if (self instanceof Player player
                && !player.isCrouching()
                && fluidState.is(FluidTags.LAVA)
                && GranbellsFurnaceEffects.isEquipped(player)) {
            cir.setReturnValue(true);
        }
    }
}

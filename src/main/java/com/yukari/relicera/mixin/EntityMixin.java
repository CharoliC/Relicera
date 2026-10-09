package com.yukari.relicera.mixin;

import com.yukari.relicera.common.item.TempestsReinsEffects;
import com.yukari.relicera.common.item.silkofnight.SilkOfNightEffects;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Inject(method = "playSound(Lnet/minecraft/sounds/SoundEvent;FF)V", at = @At("HEAD"), cancellable = true)
    private void relicera$suppressSilkOfNightMovementSounds(SoundEvent sound, float volume, float pitch,
                                                            CallbackInfo ci) {
        if ((Object) this instanceof Player player
                && SilkOfNightEffects.shouldSuppressMovementSound(player, sound)) {
            ci.cancel();
        }
    }

    @Inject(method = "isInWater", at = @At("HEAD"), cancellable = true)
    private void relicera$ignoreWaterStateForTempestsReinsWaterWalking(CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof LivingEntity livingEntity && TempestsReinsEffects.shouldIgnoreWaterState(livingEntity)) {
            cir.setReturnValue(false);
        }
    }
}

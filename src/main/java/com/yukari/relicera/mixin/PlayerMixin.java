package com.yukari.relicera.mixin;

import com.yukari.relicera.common.curio.TwoHandedSwordBroochEffects;
import com.yukari.relicera.common.item.silkofnight.SilkOfNightDecoration;
import com.yukari.relicera.common.item.silkofnight.SilkOfNightFallContext;
import com.yukari.relicera.registry.ModItems;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerMixin implements SilkOfNightFallContext {
    @Unique
    private float relicera$primaryAttackDamage;
    @Unique
    private boolean relicera$takingFallDamage;

    @Inject(method = "isScoping", at = @At("RETURN"), cancellable = true)
    private void relicera$recognizeLuminasCelestialLens(CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ()) {
            return;
        }

        Player self = (Player) (Object) this;
        cir.setReturnValue(self.isUsingItem() && self.getUseItem().is(ModItems.LUMINAS_CELESTIAL_LENS.get()));
    }

    @Inject(method = "getMovementEmission", at = @At("HEAD"), cancellable = true)
    private void relicera$suppressMovementEmission(CallbackInfoReturnable<Entity.MovementEmission> cir) {
        Player self = (Player) (Object) this;
        if (SilkOfNightDecoration.isWearing(self)) {
            cir.setReturnValue(Entity.MovementEmission.NONE);
        }
    }

    @Inject(method = "causeFallDamage", at = @At("HEAD"))
    private void relicera$beginFallDamage(float distance, float multiplier, DamageSource source,
                                          CallbackInfoReturnable<Boolean> cir) {
        relicera$takingFallDamage = SilkOfNightDecoration.isWearing((Player) (Object) this);
    }

    @Inject(method = "causeFallDamage", at = @At("RETURN"))
    private void relicera$endFallDamage(float distance, float multiplier, DamageSource source,
                                        CallbackInfoReturnable<Boolean> cir) {
        relicera$takingFallDamage = false;
    }

    @Override
    public boolean relicera$isTakingFallDamage() {
        return relicera$takingFallDamage;
    }

    @Inject(method = "attack", at = @At("HEAD"))
    private void relicera$resetPrimaryAttackDamage(Entity target, CallbackInfo ci) {
        relicera$primaryAttackDamage = 0.0F;
    }

    @ModifyArg(
            method = "attack",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"
            )
    )
    private float relicera$capturePrimaryAttackDamage(float damage) {
        relicera$primaryAttackDamage = damage;
        return damage;
    }

    @ModifyArg(
            method = "attack",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"
            )
    )
    private float relicera$applyFullSweepDamage(float damage) {
        Player self = (Player) (Object) this;
        if (relicera$primaryAttackDamage > 0.0F && TwoHandedSwordBroochEffects.hasSwordActive(self)) {
            return relicera$primaryAttackDamage;
        }
        return damage;
    }
}

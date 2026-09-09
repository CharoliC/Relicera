package com.yukari.relicera.mixin;

import com.yukari.relicera.common.curio.VindicatorsMedalEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.sensing.VillagerHostilesSensor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(VillagerHostilesSensor.class)
public abstract class VillagerHostilesSensorMixin {
    private static final double VINDICATOR_DETECTION_RANGE_SQUARED = 10.0D * 10.0D;

    @Inject(method = "isMatchingEntity", at = @At("HEAD"), cancellable = true)
    private void relicera$treatVindicatorsMedalWearersAsHostile(LivingEntity villager,
                                                                LivingEntity nearbyEntity,
                                                                CallbackInfoReturnable<Boolean> cir) {
        if (VindicatorsMedalEffects.isEquipped(nearbyEntity)
                && nearbyEntity.distanceToSqr(villager) <= VINDICATOR_DETECTION_RANGE_SQUARED) {
            cir.setReturnValue(true);
        }
    }
}

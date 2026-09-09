package com.yukari.relicera.mixin;

import com.yukari.relicera.common.curio.RingOfSatietyEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(FoodData.class)
public abstract class FoodDataMixin {
    @ModifyConstant(method = "tick", constant = @Constant(intValue = 18))
    private int relicera$lowerNaturalRegenerationThreshold(int original, Player player) {
        return RingOfSatietyEffects.isActive(player) ? 14 : original;
    }

    @ModifyConstant(method = "tick", constant = @Constant(intValue = 80, ordinal = 0))
    private int relicera$accelerateNaturalRegeneration(int original, Player player) {
        return RingOfSatietyEffects.isActive(player) && player.getFoodData().getFoodLevel() >= 16
                ? 10
                : original;
    }

    @Redirect(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/lang/Math;min(FF)F"
            )
    )
    private float relicera$keepSaturationRegenerationConsistent(float saturation, float maximum,
                                                                 Player player) {
        return RingOfSatietyEffects.isActive(player) ? maximum : Math.min(saturation, maximum);
    }
}

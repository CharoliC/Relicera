package com.yukari.relicera.mixin;

import com.yukari.relicera.common.item.armor.devilsbearskin.DevilsBearskinEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnchantmentHelper.class)
public abstract class EnchantmentHelperMixin {
    @Inject(method = "hasBindingCurse", at = @At("RETURN"), cancellable = true)
    private static void relicera$lockDevilsBearskin(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue() && DevilsBearskinEffects.isLocked(stack)) {
            cir.setReturnValue(true);
        }
    }
}

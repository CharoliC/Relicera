package com.yukari.relicera.mixin;

import com.yukari.relicera.common.curio.BaromsCovenantStoneEffects;
import java.util.function.Consumer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class BaromItemStackMixin {
    @Inject(method = "hurtAndBreak", at = @At("HEAD"), cancellable = true)
    private <T extends LivingEntity> void relicera$protectBaromTools(
            int amount, T wearer, Consumer<T> onBreak, CallbackInfo ci) {
        if (amount > 0 && BaromsCovenantStoneEffects.protectsTool(wearer, (ItemStack) (Object) this)) {
            ci.cancel();
        }
    }

    @Inject(method = "hurt", at = @At("HEAD"), cancellable = true)
    private void relicera$protectDirectBaromToolDamage(
            int amount, RandomSource random, ServerPlayer player, CallbackInfoReturnable<Boolean> cir) {
        if (amount > 0 && player != null
                && BaromsCovenantStoneEffects.protectsTool(player, (ItemStack) (Object) this)) {
            cir.setReturnValue(false);
        }
    }
}

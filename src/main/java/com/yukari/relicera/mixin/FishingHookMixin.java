package com.yukari.relicera.mixin;

import com.yukari.relicera.common.curio.FourfoldSherdPendantEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FishingHook.class)
public abstract class FishingHookMixin {
    @Mutable
    @Final
    @Shadow
    private int luck;

    @Inject(method = "<init>(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/level/Level;II)V", at = @At("TAIL"))
    private void relicera$addFourfoldSherdPendantAnglerLuck(Player player, Level level, int luck, int lureSpeed,
                                                            CallbackInfo ci) {
        this.luck += FourfoldSherdPendantEffects.countEquipped(
                player,
                FourfoldSherdPendantEffects.SherdPattern.ANGLER
        );
    }
}

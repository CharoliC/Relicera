package com.yukari.relicera.mixin;

import com.yukari.relicera.common.item.RippleheartPearlDrops;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Axolotl.class)
public abstract class AxolotlMixin {
    @Inject(method = "applySupportingEffects", at = @At("TAIL"))
    private void relicera$dropRippleheartPearl(Player player, CallbackInfo ci) {
        RippleheartPearlDrops.tryDropFromAxolotlAssist((Axolotl) (Object) this, player);
    }
}

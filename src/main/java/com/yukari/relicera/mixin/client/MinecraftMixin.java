package com.yukari.relicera.mixin.client;

import com.yukari.relicera.client.event.StonewallGreatshieldClientInput;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @Inject(method = "handleKeybinds", at = @At("HEAD"))
    private void relicera$handleStonewallGreatshieldBash(CallbackInfo ci) {
        StonewallGreatshieldClientInput.handleAttackClicks((Minecraft) (Object) this);
    }
}

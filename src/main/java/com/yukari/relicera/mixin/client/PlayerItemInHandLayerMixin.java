package com.yukari.relicera.mixin.client;

import com.yukari.relicera.registry.ModItems;
import net.minecraft.client.renderer.entity.layers.PlayerItemInHandLayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PlayerItemInHandLayer.class)
public abstract class PlayerItemInHandLayerMixin {
    @Redirect(
            method = "renderArmWithItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/world/item/Item;)Z",
                    ordinal = 0
            )
    )
    private boolean relicera$recognizeLuminasCelestialLens(ItemStack stack, Item expectedItem) {
        return stack.is(expectedItem) || stack.is(ModItems.LUMINAS_CELESTIAL_LENS.get());
    }
}

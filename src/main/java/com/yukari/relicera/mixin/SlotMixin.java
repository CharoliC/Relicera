package com.yukari.relicera.mixin;

import com.yukari.relicera.common.curio.TwoHandedSwordBroochEffects;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Slot.class)
public abstract class SlotMixin {
    @Inject(method = "mayPlace", at = @At("HEAD"), cancellable = true)
    private void relicera$preventOffhandPlacement(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        Slot self = (Slot) (Object) this;
        if (self.container instanceof Inventory inventory
                && self.getContainerSlot() == Inventory.SLOT_OFFHAND
                && TwoHandedSwordBroochEffects.isEquipped(inventory.player)) {
            cir.setReturnValue(false);
        }
    }
}

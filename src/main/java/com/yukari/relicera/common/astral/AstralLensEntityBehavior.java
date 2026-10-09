package com.yukari.relicera.common.astral;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

public final class AstralLensEntityBehavior {
    private static final double VOID_RECOVERY_Y_OFFSET = 60.0D;

    private AstralLensEntityBehavior() {
    }

    public static boolean tick(ItemEntity itemEntity) {
        itemEntity.setGlowingTag(true);
        AstralLensDropSequence.tick(itemEntity);

        if (!itemEntity.level().isClientSide && shouldRecoverFromVoid(itemEntity)) {
            recoverFromVoid(itemEntity);
            return true;
        }

        return false;
    }

    private static boolean shouldRecoverFromVoid(ItemEntity itemEntity) {
        return itemEntity.getY() < itemEntity.level().getMinBuildHeight() - VOID_RECOVERY_Y_OFFSET;
    }

    private static void recoverFromVoid(ItemEntity itemEntity) {
        if (!(itemEntity.level() instanceof net.minecraft.server.level.ServerLevel)) {
            itemEntity.discard();
            return;
        }

        ItemStack lens = itemEntity.getItem().copy();
        Entity owner = itemEntity.getOwner();
        if (!(owner instanceof ServerPlayer ownerPlayer)) {
            itemEntity.discard();
            return;
        }

        lens = ownerPlayer.getEnderChestInventory().addItem(lens);
        if (!lens.isEmpty() && ownerPlayer.getInventory().add(lens)) {
            lens = ItemStack.EMPTY;
        }

        if (lens.isEmpty()) {
            itemEntity.discard();
            return;
        }

        itemEntity.discard();
    }
}

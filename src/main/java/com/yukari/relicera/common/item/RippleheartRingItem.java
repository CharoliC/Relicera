package com.yukari.relicera.common.item;

import java.util.Optional;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;

public class RippleheartRingItem extends QuickEquipCurioItem {
    private static final String PAIR_ID_TAG = "RippleheartPairId";

    private final RingSide side;

    public RippleheartRingItem(Properties properties, RingSide side) {
        super(properties);
        this.side = side;
    }

    public RingSide getSide() {
        return side;
    }

    @Override
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        if (!super.canEquip(slotContext, stack)) {
            return false;
        }
        if (slotContext.entity() == null) {
            return true;
        }

        return CuriosApi.getCuriosInventory(slotContext.entity())
                .resolve()
                .map(handler -> handler.findCurios(equipped -> equipped.getItem() instanceof RippleheartRingItem)
                        .stream()
                        .noneMatch(result -> result.stack() != stack))
                .orElse(true);
    }

    public ItemStack createBoundStack(UUID pairId) {
        ItemStack stack = new ItemStack(this);
        setPairId(stack, pairId);
        return stack;
    }

    public static void setPairId(ItemStack stack, UUID pairId) {
        if (isPairItem(stack)) {
            stack.getOrCreateTag().putUUID(PAIR_ID_TAG, pairId);
        }
    }

    public static Optional<UUID> getPairId(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (!isPairItem(stack) || tag == null || !tag.hasUUID(PAIR_ID_TAG)) {
            return Optional.empty();
        }
        return Optional.of(tag.getUUID(PAIR_ID_TAG));
    }

    private static boolean isPairItem(ItemStack stack) {
        return stack.getItem() instanceof RippleheartRingItem
                || stack.getItem() instanceof RippleheartRingsItem;
    }

    public enum RingSide {
        RED,
        BLUE
    }
}

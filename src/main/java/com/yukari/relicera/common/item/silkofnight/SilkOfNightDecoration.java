package com.yukari.relicera.common.item.silkofnight;

import com.yukari.relicera.registry.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.AnvilUpdateEvent;

public final class SilkOfNightDecoration {
    private static final String DECORATION_KEY = "ReliceraSilkOfNightDecoration";
    private static final int EXPERIENCE_LEVEL_COST = 10;

    private SilkOfNightDecoration() {
    }

    public static void apply(ItemStack stack) {
        stack.getOrCreateTag().putBoolean(DECORATION_KEY, true);
    }

    public static boolean hasDecoration(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.getBoolean(DECORATION_KEY);
    }

    public static boolean isWearing(Player player) {
        return hasDecoration(player.getItemBySlot(EquipmentSlot.FEET));
    }

    public static void updateAnvilResult(AnvilUpdateEvent event) {
        ItemStack left = event.getLeft();
        ItemStack right = event.getRight();
        if (!right.is(ModItems.SILK_OF_NIGHT.get()) || !canDecorate(left)) {
            return;
        }

        ItemStack output = left.copy();
        apply(output);
        event.setOutput(output);
        event.setCost(EXPERIENCE_LEVEL_COST);
        event.setMaterialCost(1);
    }

    private static boolean canDecorate(ItemStack stack) {
        return stack.getItem() instanceof ArmorItem armorItem
                && armorItem.getType() == ArmorItem.Type.BOOTS
                && !hasDecoration(stack);
    }
}

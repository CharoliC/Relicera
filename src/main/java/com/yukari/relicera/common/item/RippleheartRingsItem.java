package com.yukari.relicera.common.item;

import com.yukari.relicera.registry.ModItems;
import java.util.UUID;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class RippleheartRingsItem extends Item {
    public RippleheartRingsItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide()) {
            UUID pairId = RippleheartRingItem.getPairId(stack).orElseGet(UUID::randomUUID);
            ItemStack redRing = ModItems.RIPPLEHEART_RING_RED.get().createBoundStack(pairId);
            ItemStack blueRing = ModItems.RIPPLEHEART_RING_BLUE.get().createBoundStack(pairId);

            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            giveOrDrop(player, redRing);
            giveOrDrop(player, blueRing);
            player.awardStat(Stats.ITEM_USED.get(this));
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    private static void giveOrDrop(Player player, ItemStack stack) {
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
    }
}

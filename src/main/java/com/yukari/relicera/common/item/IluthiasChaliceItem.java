package com.yukari.relicera.common.item;

import com.yukari.relicera.common.tooltip.IluthiasChaliceTooltip;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;

public class IluthiasChaliceItem extends RelicCurioItem {
    private static final String TAG_TOTEMS = "Totems";
    public static final int TOTEM_CAPACITY = 5;

    public IluthiasChaliceItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack chalice, ItemStack carried, Slot slot, ClickAction action, Player player, SlotAccess carriedSlot) {
        if (chalice.getCount() != 1 || action != ClickAction.SECONDARY || !slot.allowModification(player)) {
            return false;
        }

        if (carried.isEmpty()) {
            Optional<ItemStack> removed = removeOneTotem(chalice);
            if (removed.isPresent() && carriedSlot.set(removed.get())) {
                playRemoveOneSound(player);
            }
            return true;
        }

        if (carried.is(Items.TOTEM_OF_UNDYING) && addOneTotem(chalice)) {
            carried.shrink(1);
            playInsertSound(player);
        }
        return true;
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        return Optional.of(new IluthiasChaliceTooltip(getTotems(stack), TOTEM_CAPACITY));
    }

    @Override
    public void onDestroyed(ItemEntity itemEntity) {
        ItemUtils.onContainerDestroyed(itemEntity, getTotemContents(itemEntity.getItem()));
    }

    public static boolean addOneTotem(ItemStack chalice) {
        if (!chalice.is(Items.AIR) && getTotemCount(chalice) < TOTEM_CAPACITY) {
            ListTag totems = getOrCreateTotems(chalice);
            CompoundTag savedTotem = new CompoundTag();
            new ItemStack(Items.TOTEM_OF_UNDYING).save(savedTotem);
            totems.add(0, savedTotem);
            return true;
        }
        return false;
    }

    public static Optional<ItemStack> peekOneTotem(ItemStack chalice) {
        ListTag totems = getTotemList(chalice);
        if (totems.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(ItemStack.of(totems.getCompound(0)));
    }

    public static Optional<ItemStack> removeOneTotem(ItemStack chalice) {
        CompoundTag tag = chalice.getTag();
        if (tag == null || !tag.contains(TAG_TOTEMS, Tag.TAG_LIST)) {
            return Optional.empty();
        }

        ListTag totems = tag.getList(TAG_TOTEMS, Tag.TAG_COMPOUND);
        if (totems.isEmpty()) {
            return Optional.empty();
        }

        ItemStack removed = ItemStack.of(totems.getCompound(0));
        totems.remove(0);
        if (totems.isEmpty()) {
            chalice.removeTagKey(TAG_TOTEMS);
        }
        return Optional.of(removed);
    }

    public static NonNullList<ItemStack> getTotems(ItemStack chalice) {
        NonNullList<ItemStack> result = NonNullList.create();
        getTotemContents(chalice).forEach(result::add);
        return result;
    }

    public static Stream<ItemStack> getTotemContents(ItemStack chalice) {
        return getTotemList(chalice).stream()
                .filter(CompoundTag.class::isInstance)
                .map(CompoundTag.class::cast)
                .map(ItemStack::of)
                .filter(stack -> !stack.isEmpty() && stack.is(Items.TOTEM_OF_UNDYING));
    }

    private static int getTotemCount(ItemStack chalice) {
        return (int) getTotemContents(chalice).count();
    }

    private static ListTag getOrCreateTotems(ItemStack chalice) {
        CompoundTag tag = chalice.getOrCreateTag();
        if (!tag.contains(TAG_TOTEMS, Tag.TAG_LIST)) {
            tag.put(TAG_TOTEMS, new ListTag());
        }
        return tag.getList(TAG_TOTEMS, Tag.TAG_COMPOUND);
    }

    private static ListTag getTotemList(ItemStack chalice) {
        CompoundTag tag = chalice.getTag();
        if (tag == null || !tag.contains(TAG_TOTEMS, Tag.TAG_LIST)) {
            return new ListTag();
        }
        return tag.getList(TAG_TOTEMS, Tag.TAG_COMPOUND);
    }

    private static void playInsertSound(Entity entity) {
        entity.playSound(SoundEvents.BUNDLE_INSERT, 0.8F, 0.8F + entity.level().getRandom().nextFloat() * 0.4F);
    }

    private static void playRemoveOneSound(Entity entity) {
        entity.playSound(SoundEvents.BUNDLE_REMOVE_ONE, 0.8F, 0.8F + entity.level().getRandom().nextFloat() * 0.4F);
    }
}

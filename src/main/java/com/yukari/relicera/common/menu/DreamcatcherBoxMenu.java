package com.yukari.relicera.common.menu;

import com.yukari.relicera.ReliceraMod;
import com.yukari.relicera.common.block.DreamcatcherBoxBlockEntity;
import net.minecraft.advancements.Advancement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class DreamcatcherBoxMenu extends AbstractContainerMenu {
    private static final int CONTAINER_SLOT_COUNT = DreamcatcherBoxBlockEntity.SLOT_COUNT;
    private static final int PLAYER_INVENTORY_START = CONTAINER_SLOT_COUNT;
    private static final int PLAYER_INVENTORY_END = PLAYER_INVENTORY_START + 27;
    private static final int HOTBAR_START = PLAYER_INVENTORY_END;
    private static final int HOTBAR_END = HOTBAR_START + 9;
    private static final ResourceLocation LUMINAS_EXTRA_1 =
            ResourceLocation.fromNamespaceAndPath(ReliceraMod.MOD_ID, "luminas_extra_1");

    private final Container container;

    public DreamcatcherBoxMenu(int containerId, Inventory playerInventory, Container container) {
        super(MenuType.GENERIC_9x3, containerId);
        checkContainerSize(container, CONTAINER_SLOT_COUNT);
        this.container = container;
        container.startOpen(playerInventory.player);

        addBoxSlots(container);
        addPlayerInventorySlots(playerInventory);
    }

    private void addBoxSlots(Container container) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new DreamcatcherBoxSlot(container, column + row * 9, 8 + column * 18, 18 + row * 18));
            }
        }
    }

    private void addPlayerInventorySlots(Inventory playerInventory) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(playerInventory, column + row * 9 + 9, 8 + column * 18, 84 + row * 18));
            }
        }

        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(playerInventory, column, 8 + column * 18, 142));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) {
            return result;
        }

        ItemStack stack = slot.getItem();
        result = stack.copy();

        if (index < CONTAINER_SLOT_COUNT) {
            if (!moveItemStackTo(stack, PLAYER_INVENTORY_START, HOTBAR_END, true)) {
                return ItemStack.EMPTY;
            }
            awardCakeAdvancement(player, result);
        } else if (!moveItemStackTo(stack, 0, CONTAINER_SLOT_COUNT, false)) {
            return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        slot.onTake(player, stack);
        return result;
    }

    @Override
    public boolean stillValid(Player player) {
        return container.stillValid(player);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        container.stopOpen(player);
    }

    private static void awardCakeAdvancement(Player player, ItemStack stack) {
        if (!(player instanceof ServerPlayer serverPlayer) || !stack.is(Items.CAKE)) {
            return;
        }

        Advancement advancement = serverPlayer.server.getAdvancements().getAdvancement(LUMINAS_EXTRA_1);
        if (advancement != null) {
            serverPlayer.getAdvancements().award(advancement, "cake_from_dreamcatcher_box");
        }
    }

    private static class DreamcatcherBoxSlot extends Slot {
        private DreamcatcherBoxSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public void onTake(Player player, ItemStack stack) {
            awardCakeAdvancement(player, stack);
            super.onTake(player, stack);
        }
    }
}

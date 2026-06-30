package com.yukari.relicera.common.menu;

import com.yukari.relicera.common.item.FourfoldSherdPendantItem;
import com.yukari.relicera.registry.ModItems;
import com.yukari.relicera.registry.ModMenuTypes;
import com.yukari.relicera.registry.ModTags;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

public class FourfoldSherdPendantMenu extends AbstractContainerMenu {
    private static final int SHERD_SLOT_COUNT = FourfoldSherdPendantItem.SHERD_SLOT_COUNT;
    private static final int PLAYER_INVENTORY_START = SHERD_SLOT_COUNT;
    private static final int PLAYER_INVENTORY_END = PLAYER_INVENTORY_START + 27;
    private static final int HOTBAR_START = PLAYER_INVENTORY_END;
    private static final int HOTBAR_END = HOTBAR_START + 9;

    private final ItemStack pendantStack;

    public FourfoldSherdPendantMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory, playerInventory.player.getMainHandItem());
    }

    public FourfoldSherdPendantMenu(int containerId, Inventory playerInventory, ItemStack pendantStack) {
        super(ModMenuTypes.FOURFOLD_SHERD_PENDANT.get(), containerId);
        this.pendantStack = pendantStack;

        addPendantSlots(new PendantSherdHandler(pendantStack));
        addPlayerInventorySlots(playerInventory);
    }

    private void addPendantSlots(ItemStackHandler itemHandler) {
        this.addSlot(new SherdSlot(itemHandler, 0, 26, 31)); //left top + 1
        this.addSlot(new SherdSlot(itemHandler, 1, 62, 31));
        this.addSlot(new SherdSlot(itemHandler, 2, 98, 31));
        this.addSlot(new SherdSlot(itemHandler, 3, 134, 31));
    }

    private void addPlayerInventorySlots(Inventory playerInventory) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                this.addSlot(new net.minecraft.world.inventory.Slot(playerInventory, column + row * 9 + 9, 8 + column * 18, 84 + row * 18));
            }
        }

        for (int column = 0; column < 9; column++) {
            this.addSlot(new net.minecraft.world.inventory.Slot(playerInventory, column, 8 + column * 18, 142));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        var slot = this.slots.get(index);
        if (!slot.hasItem()) {
            return result;
        }

        ItemStack stack = slot.getItem();
        result = stack.copy();

        if (index < SHERD_SLOT_COUNT) {
            if (!this.moveItemStackTo(stack, PLAYER_INVENTORY_START, HOTBAR_END, true)) {
                return ItemStack.EMPTY;
            }
        } else if (isSherd(stack)) {
            if (!this.moveItemStackTo(stack, 0, SHERD_SLOT_COUNT, false)) {
                return ItemStack.EMPTY;
            }
        } else if (index < HOTBAR_START) {
            if (!this.moveItemStackTo(stack, HOTBAR_START, HOTBAR_END, false)) {
                return ItemStack.EMPTY;
            }
        } else if (!this.moveItemStackTo(stack, PLAYER_INVENTORY_START, PLAYER_INVENTORY_END, false)) {
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
        return player.getMainHandItem() == pendantStack && pendantStack.is(ModItems.FOURFOLD_SHERD_PENDANT.get());
    }

    public static boolean isSherd(ItemStack stack) {
        return stack.is(ModTags.DECORATED_POT_SHERDS);
    }

    private static class SherdSlot extends SlotItemHandler {
        private SherdSlot(ItemStackHandler itemHandler, int index, int xPosition, int yPosition) {
            super(itemHandler, index, xPosition, yPosition);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return isSherd(stack);
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }
    }

    private static class PendantSherdHandler extends ItemStackHandler {
        private final ItemStack pendantStack;

        private PendantSherdHandler(ItemStack pendantStack) {
            super(SHERD_SLOT_COUNT);
            this.pendantStack = pendantStack;
            if (pendantStack.hasTag() && pendantStack.getTag().contains(FourfoldSherdPendantItem.SHERDS_TAG)) {
                deserializeNBT(pendantStack.getTag().getCompound(FourfoldSherdPendantItem.SHERDS_TAG));
            }
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return isSherd(stack);
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }

        @Override
        protected void onContentsChanged(int slot) {
            pendantStack.getOrCreateTag().put(FourfoldSherdPendantItem.SHERDS_TAG, serializeNBT());
        }
    }
}

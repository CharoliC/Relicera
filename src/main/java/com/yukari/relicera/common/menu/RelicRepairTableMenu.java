package com.yukari.relicera.common.menu;

import com.yukari.relicera.common.block.RelicRepairTableBlockEntity;
import com.yukari.relicera.registry.ModBlocks;
import com.yukari.relicera.registry.ModItems;
import com.yukari.relicera.registry.ModMenuTypes;
import com.yukari.relicera.registry.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

public class RelicRepairTableMenu extends AbstractContainerMenu {
    private static final int CONTAINER_SLOT_COUNT = RelicRepairTableBlockEntity.SLOT_COUNT;
    private static final int PLAYER_INVENTORY_START = CONTAINER_SLOT_COUNT;
    private static final int PLAYER_INVENTORY_END = PLAYER_INVENTORY_START + 27;
    private static final int HOTBAR_START = PLAYER_INVENTORY_END;
    private static final int HOTBAR_END = HOTBAR_START + 9;

    private final ContainerLevelAccess access;
    private final ContainerData data;

    public RelicRepairTableMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory, getBlockEntity(playerInventory.player.level(), extraData.readBlockPos()));
    }

    public RelicRepairTableMenu(int containerId, Inventory playerInventory, RelicRepairTableBlockEntity blockEntity) {
        super(ModMenuTypes.RELIC_REPAIR_TABLE.get(), containerId);
        this.data = blockEntity.getDataAccess();
        this.access = ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos());

        ItemStackHandler itemHandler = blockEntity.getItemHandler();
        addBlockSlots(itemHandler);
        addPlayerInventorySlots(playerInventory);
        addDataSlots(data);
    }

    private void addBlockSlots(ItemStackHandler itemHandler) {
        this.addSlot(new SlotItemHandler(itemHandler, RelicRepairTableBlockEntity.SLOT_FEYSILVER, 54, 21) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(ModItems.FEYSILVER_INGOT.get());
            }
        });

        this.addSlot(new SlotItemHandler(itemHandler, RelicRepairTableBlockEntity.SLOT_RELIC, 80, 12) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return isRelicSlotItem(stack);
            }
        });

        this.addSlot(new SlotItemHandler(itemHandler, RelicRepairTableBlockEntity.SLOT_MATERIAL_1, 106, 21));
        this.addSlot(new SlotItemHandler(itemHandler, RelicRepairTableBlockEntity.SLOT_MATERIAL_2, 45, 45));
        this.addSlot(new SlotItemHandler(itemHandler, RelicRepairTableBlockEntity.SLOT_MATERIAL_3, 115, 45));

        this.addSlot(new SlotItemHandler(itemHandler, RelicRepairTableBlockEntity.SLOT_OUTPUT, 80, 54) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });
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

        if (index < CONTAINER_SLOT_COUNT) {
            if (!this.moveItemStackTo(stack, PLAYER_INVENTORY_START, HOTBAR_END, true)) {
                return ItemStack.EMPTY;
            }
        } else if (stack.is(ModItems.FEYSILVER_INGOT.get())) {
            if (!this.moveItemStackTo(stack, RelicRepairTableBlockEntity.SLOT_FEYSILVER, RelicRepairTableBlockEntity.SLOT_FEYSILVER + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (isRelicSlotItem(stack)) {
            if (!this.moveItemStackTo(stack, RelicRepairTableBlockEntity.SLOT_RELIC, RelicRepairTableBlockEntity.SLOT_RELIC + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (!this.moveItemStackTo(stack, RelicRepairTableBlockEntity.SLOT_MATERIAL_1, RelicRepairTableBlockEntity.SLOT_OUTPUT, false)) {
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
        return stillValid(access, player, ModBlocks.RELIC_REPAIR_TABLE.get());
    }

    public ItemStack getRepairSlotItem(int slot) {
        if (slot < 0 || slot >= CONTAINER_SLOT_COUNT) {
            return ItemStack.EMPTY;
        }
        return this.slots.get(slot).getItem();
    }

    public int getRepairProgress() {
        return data.get(0);
    }

    public int getRepairTime() {
        return data.get(1);
    }

    public int getScaledRepairProgress(int size) {
        int repairTime = getRepairTime();
        if (repairTime <= 0 || getRepairProgress() <= 0) {
            return 0;
        }
        return Math.min(size, getRepairProgress() * size / repairTime);
    }

    public static boolean isRelicSlotItem(ItemStack stack) {
        return stack.is(ModTags.BROKEN_RELICS) || stack.is(ModTags.REPAIRED_RELICS);
    }

    private static RelicRepairTableBlockEntity getBlockEntity(Level level, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof RelicRepairTableBlockEntity relicRepairTable) {
            return relicRepairTable;
        }
        throw new IllegalStateException("Expected relic repair table block entity at " + pos);
    }
}

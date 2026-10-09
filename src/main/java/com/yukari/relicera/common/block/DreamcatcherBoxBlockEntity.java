package com.yukari.relicera.common.block;

import com.yukari.relicera.common.menu.DreamcatcherBoxMenu;
import com.yukari.relicera.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

import java.util.List;

public class DreamcatcherBoxBlockEntity extends BlockEntity implements MenuProvider, Container {
    public static final int SLOT_COUNT = 27;

    private NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
    private int openCount;

    public DreamcatcherBoxBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.DREAMCATCHER_BOX.get(), pos, state);
    }

    @Override
    public int getContainerSize() {
        return SLOT_COUNT;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack stack = ContainerHelper.removeItem(items, slot, amount);
        if (!stack.isEmpty()) {
            contentChanged();
        }
        return stack;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        items.set(slot, stack);
        if (stack.getCount() > getMaxStackSize()) {
            stack.setCount(getMaxStackSize());
        }
        contentChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        return level != null
                && level.getBlockEntity(worldPosition) == this
                && player.distanceToSqr(
                (double) worldPosition.getX() + 0.5D,
                (double) worldPosition.getY() + 0.5D,
                (double) worldPosition.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public void clearContent() {
        items.clear();
        contentChanged();
    }

    public boolean canInsertAll(List<ItemStack> stacks) {
        NonNullList<ItemStack> simulated = NonNullList.withSize(getContainerSize(), ItemStack.EMPTY);
        for (int slot = 0; slot < getContainerSize(); slot++) {
            simulated.set(slot, items.get(slot).copy());
        }

        for (ItemStack stack : stacks) {
            if (!insertInto(simulated, stack.copy())) {
                return false;
            }
        }
        return true;
    }

    public boolean insertAll(List<ItemStack> stacks) {
        if (!canInsertAll(stacks)) {
            return false;
        }

        for (ItemStack stack : stacks) {
            insertInto(items, stack.copy());
        }
        contentChanged();
        return true;
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        items = NonNullList.withSize(getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, items);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ContainerHelper.saveAllItems(tag, items);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.relicera.dreamcatcher_box");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new DreamcatcherBoxMenu(containerId, playerInventory, this);
    }

    @Override
    public void startOpen(Player player) {
        if (!isRemoved() && !player.isSpectator()) {
            if (openCount++ == 0) {
                setOpen(true);
            }
        }
    }

    @Override
    public void stopOpen(Player player) {
        if (!isRemoved() && !player.isSpectator()) {
            openCount = Math.max(0, openCount - 1);
            if (openCount == 0) {
                setOpen(false);
            }
        }
    }

    private void setOpen(boolean open) {
        if (level == null || level.isClientSide) {
            return;
        }

        BlockState state = getBlockState();
        if (!state.hasProperty(DreamcatcherBoxBlock.OPEN) || state.getValue(DreamcatcherBoxBlock.OPEN) == open) {
            return;
        }

        level.setBlock(worldPosition, state.setValue(DreamcatcherBoxBlock.OPEN, open), Block.UPDATE_CLIENTS);
        level.playSound(null, worldPosition, open ? SoundEvents.BARREL_OPEN : SoundEvents.BARREL_CLOSE, SoundSource.BLOCKS, 0.5F, level.random.nextFloat() * 0.1F + 0.9F);
        level.gameEvent(null, open ? GameEvent.CONTAINER_OPEN : GameEvent.CONTAINER_CLOSE, worldPosition);
    }

    private void contentChanged() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.updateNeighbourForOutputSignal(worldPosition, getBlockState().getBlock());
        }
    }

    private boolean insertInto(NonNullList<ItemStack> target, ItemStack stack) {
        if (stack.isEmpty()) {
            return true;
        }

        for (int slot = 0; slot < target.size(); slot++) {
            ItemStack existing = target.get(slot);
            if (!existing.isEmpty() && ItemStack.isSameItemSameTags(existing, stack)) {
                int maxStackSize = Math.min(getMaxStackSize(), existing.getMaxStackSize());
                int transfer = Math.min(stack.getCount(), maxStackSize - existing.getCount());
                if (transfer > 0) {
                    existing.grow(transfer);
                    stack.shrink(transfer);
                    if (stack.isEmpty()) {
                        return true;
                    }
                }
            }
        }

        for (int slot = 0; slot < target.size(); slot++) {
            if (target.get(slot).isEmpty()) {
                int transfer = Math.min(stack.getCount(), Math.min(getMaxStackSize(), stack.getMaxStackSize()));
                ItemStack inserted = stack.copy();
                inserted.setCount(transfer);
                target.set(slot, inserted);
                stack.shrink(transfer);
                if (stack.isEmpty()) {
                    return true;
                }
            }
        }

        return false;
    }
}

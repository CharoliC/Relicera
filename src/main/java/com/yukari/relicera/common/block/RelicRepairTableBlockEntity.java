package com.yukari.relicera.common.block;

import com.yukari.relicera.common.menu.RelicRepairTableMenu;
import com.yukari.relicera.common.recipe.RelicRepairRecipe;
import com.yukari.relicera.common.recipe.RelicRepairRecipes;
import com.yukari.relicera.registry.ModBlockEntities;
import com.yukari.relicera.registry.ModItems;
import com.yukari.relicera.registry.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

public class RelicRepairTableBlockEntity extends BlockEntity implements MenuProvider {
    public static final int SLOT_FEYSILVER = 0;
    public static final int SLOT_RELIC = 1;
    public static final int SLOT_MATERIAL_1 = 2;
    public static final int SLOT_MATERIAL_2 = 3;
    public static final int SLOT_MATERIAL_3 = 4;
    public static final int SLOT_OUTPUT = 5;
    public static final int SLOT_COUNT = 6;

    private static final String ITEMS_TAG = "Items";
    private static final String REPAIR_PROGRESS_TAG = "RepairProgress";
    private static final int AMBIENT_WITCH_PARTICLE_AVERAGE_INTERVAL = 80;

    private final ItemStackHandler itemHandler = new ItemStackHandler(SLOT_COUNT) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return switch (slot) {
                case SLOT_FEYSILVER -> stack.is(ModItems.FEYSILVER_INGOT.get());
                case SLOT_RELIC -> stack.is(ModTags.BROKEN_RELICS) || stack.is(ModTags.REPAIRED_RELICS);
                case SLOT_MATERIAL_1, SLOT_MATERIAL_2, SLOT_MATERIAL_3 -> true;
                case SLOT_OUTPUT -> false;
                default -> super.isItemValid(slot, stack);
            };
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            syncToClient();
        }
    };
    private final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> repairProgress;
                case 1 -> getCurrentRepairTime();
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            if (index == 0) {
                repairProgress = value;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    };
    private LazyOptional<IItemHandler> itemHandlerCapability = LazyOptional.of(() -> itemHandler);
    private int repairProgress;

    public RelicRepairTableBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RELIC_REPAIR_TABLE.get(), pos, state);
    }

    public ItemStackHandler getItemHandler() {
        return itemHandler;
    }

    public ContainerData getDataAccess() {
        return dataAccess;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.relicera.relic_repair_table");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new RelicRepairTableMenu(containerId, playerInventory, this);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains(ITEMS_TAG)) {
            itemHandler.deserializeNBT(tag.getCompound(ITEMS_TAG));
        }
        repairProgress = tag.getInt(REPAIR_PROGRESS_TAG);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put(ITEMS_TAG, itemHandler.serializeNBT());
        tag.putInt(REPAIR_PROGRESS_TAG, repairProgress);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        itemHandlerCapability.invalidate();
    }

    @Override
    public void reviveCaps() {
        super.reviveCaps();
        itemHandlerCapability = LazyOptional.of(() -> itemHandler);
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction side) {
        if (capability == ForgeCapabilities.ITEM_HANDLER) {
            return itemHandlerCapability.cast();
        }
        return super.getCapability(capability, side);
    }

    public void dropContents() {
        if (level == null) {
            return;
        }

        for (int slot = 0; slot < itemHandler.getSlots(); slot++) {
            ItemStack stack = itemHandler.getStackInSlot(slot);
            if (!stack.isEmpty()) {
                net.minecraft.world.Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), stack);
                itemHandler.setStackInSlot(slot, ItemStack.EMPTY);
            }
        }
    }

    public static void tick(Level level, BlockPos pos, BlockState state, RelicRepairTableBlockEntity blockEntity) {
        if (level.isClientSide) {
            return;
        }

        if (level instanceof ServerLevel serverLevel) {
            spawnAmbientParticles(serverLevel, pos);
        }

        boolean changed = blockEntity.tickRepair();
        if (changed) {
            setChanged(level, pos, state);
        }
    }

    private static void spawnAmbientParticles(ServerLevel level, BlockPos pos) {
        if (level.random.nextInt(AMBIENT_WITCH_PARTICLE_AVERAGE_INTERVAL) != 0) {
            return;
        }

        double x = pos.getX() + 0.35D + level.random.nextDouble() * 0.3D;
        double y = pos.getY() + 1.05D + level.random.nextDouble() * 0.25D;
        double z = pos.getZ() + 0.35D + level.random.nextDouble() * 0.3D;
        level.sendParticles(ParticleTypes.WITCH, x, y, z, 1, 0.02D, 0.02D, 0.02D, 0.0D);
    }

    private boolean tickRepair() {
        var recipe = RelicRepairRecipes.findForRelic(itemHandler.getStackInSlot(SLOT_RELIC)).orElse(null);
        if (recipe == null || !canRepair(recipe)) {
            return resetProgress();
        }

        if (level instanceof ServerLevel serverLevel) {
            recipe.particleStyle().spawnActive(serverLevel, worldPosition, repairProgress);
        }
        repairProgress++;
        if (repairProgress >= recipe.repairTime()) {
            completeRepair(recipe);
            repairProgress = 0;
        }
        return true;
    }

    private boolean canRepair(RelicRepairRecipe recipe) {
        if (itemHandler.getStackInSlot(SLOT_FEYSILVER).getCount() < recipe.feysilverCost()) {
            return false;
        }

        for (int index = 0; index < RelicRepairRecipe.MATERIAL_COUNT; index++) {
            if (!recipe.matchesMaterial(index, itemHandler.getStackInSlot(SLOT_MATERIAL_1 + index))) {
                return false;
            }
        }

        ItemStack output = itemHandler.getStackInSlot(SLOT_OUTPUT);
        return output.isEmpty();
    }

    private void completeRepair(RelicRepairRecipe recipe) {
        itemHandler.getStackInSlot(SLOT_RELIC).shrink(1);
        itemHandler.getStackInSlot(SLOT_FEYSILVER).shrink(recipe.feysilverCost());
        for (int index = SLOT_MATERIAL_1; index <= SLOT_MATERIAL_3; index++) {
            itemHandler.getStackInSlot(index).shrink(1);
        }
        itemHandler.setStackInSlot(SLOT_OUTPUT, recipe.output());
        if (level instanceof ServerLevel serverLevel) {
            recipe.particleStyle().spawnComplete(serverLevel, worldPosition);
        }
    }

    private boolean resetProgress() {
        if (repairProgress == 0) {
            return false;
        }
        repairProgress = 0;
        return true;
    }

    private int getCurrentRepairTime() {
        return RelicRepairRecipes.findForRelic(itemHandler.getStackInSlot(SLOT_RELIC))
                .map(RelicRepairRecipe::repairTime)
                .orElse(0);
    }

    private void syncToClient() {
        if (level != null && !level.isClientSide) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_CLIENTS);
        }
    }
}

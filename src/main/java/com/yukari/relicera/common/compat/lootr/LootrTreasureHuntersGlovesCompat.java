package com.yukari.relicera.common.compat.lootr;

import com.yukari.relicera.common.curio.TreasureHuntersGlovesContainerContents;
import com.yukari.relicera.common.curio.TreasureHuntersGlovesEffects;
import com.yukari.relicera.config.ModCommonConfig;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import noobanidus.mods.lootr.api.blockentity.ILootBlockEntity;
import noobanidus.mods.lootr.api.inventory.ILootrInventory;
import noobanidus.mods.lootr.data.ChestData;
import noobanidus.mods.lootr.data.DataStorage;

public final class LootrTreasureHuntersGlovesCompat {
    private static final String BLOCK_DATA_TAG = "ReliceraTreasureHuntersGlovesLootr";
    private static final String PLAYERS_KEY = "Players";
    private static final String INITIAL_CONTENTS_KEY = "InitialContents";
    private static final String REFRESHED_KEY = "Refreshed";
    private static final long PENDING_OPEN_TIMEOUT_TICKS = 5L;
    private static final Map<UUID, PendingOpen> PENDING_FIRST_OPENS = new HashMap<>();

    private LootrTreasureHuntersGlovesCompat() {
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer player)
                || !(player.level() instanceof ServerLevel level)
                || event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }

        BlockEntity blockEntity = level.getBlockEntity(event.getPos());
        if (!(blockEntity instanceof ILootBlockEntity lootBlockEntity)) {
            return;
        }

        if (player.isShiftKeyDown()
                && player.getMainHandItem().isEmpty()
                && player.getOffhandItem().isEmpty()
                && TreasureHuntersGlovesEffects.isEquipped(player)) {
            tryRefresh(level, player, blockEntity, lootBlockEntity);
            event.setCancellationResult(InteractionResult.CONSUME);
            event.setCanceled(true);
            return;
        }

        if (!player.isShiftKeyDown()
                && TreasureHuntersGlovesEffects.isEquipped(player)
                && !DataStorage.isScored(player.getUUID(), lootBlockEntity.getTileId())) {
            PENDING_FIRST_OPENS.put(player.getUUID(), new PendingOpen(
                    level.dimension(),
                    event.getPos().immutable(),
                    lootBlockEntity.getTileId(),
                    level.getGameTime()
            ));
        }
    }

    @SubscribeEvent
    public static void onContainerOpen(PlayerContainerEvent.Open event) {
        if (!(event.getEntity() instanceof ServerPlayer player)
                || !(player.level() instanceof ServerLevel level)) {
            return;
        }

        PendingOpen pending = PENDING_FIRST_OPENS.remove(player.getUUID());
        if (pending == null
                || !pending.dimension().equals(level.dimension())
                || level.getGameTime() - pending.gameTime() > PENDING_OPEN_TIMEOUT_TICKS) {
            return;
        }

        BlockEntity blockEntity = level.getBlockEntity(pending.position());
        if (!(blockEntity instanceof ILootBlockEntity lootBlockEntity)
                || !pending.containerId().equals(lootBlockEntity.getTileId())) {
            return;
        }

        ILootrInventory inventory = getPlayerInventory(level, player, lootBlockEntity);
        if (inventory == null
                || !TreasureHuntersGlovesEffects.isEquipped(player)
                || getPlayerData(blockEntity, player.getUUID()).contains(INITIAL_CONTENTS_KEY, Tag.TAG_LIST)) {
            return;
        }

        CompoundTag playerData = getOrCreatePlayerData(blockEntity, player.getUUID());
        playerData.put(
                INITIAL_CONTENTS_KEY,
                TreasureHuntersGlovesContainerContents.createSignature(inventory)
        );
        playerData.putBoolean(REFRESHED_KEY, false);
        blockEntity.setChanged();
        TreasureHuntersGlovesEffects.recordFirstOpenedLootContainer(player);
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        PENDING_FIRST_OPENS.remove(event.getEntity().getUUID());
    }

    private static void tryRefresh(ServerLevel level, ServerPlayer player, BlockEntity blockEntity,
                                   ILootBlockEntity lootBlockEntity) {
        CompoundTag playerData = getPlayerData(blockEntity, player.getUUID());
        ResourceLocation lootTableId = lootBlockEntity.getTable();
        if (lootTableId == null
                || !playerData.contains(INITIAL_CONTENTS_KEY, Tag.TAG_LIST)
                || playerData.getBoolean(REFRESHED_KEY)) {
            return;
        }

        ILootrInventory inventory = getPlayerInventory(level, player, lootBlockEntity);
        if (inventory == null
                || !TreasureHuntersGlovesContainerContents.canRefresh(
                List.of(playerData.getList(INITIAL_CONTENTS_KEY, Tag.TAG_COMPOUND)),
                List.of(inventory),
                ModCommonConfig.TREASURE_HUNTERS_GLOVES_MAX_ITEMS_TAKEN_BEFORE_REFRESH.get()
        )) {
            return;
        }

        inventory.clearContent();
        lootBlockEntity.unpackLootTable(player, inventory, lootTableId, level.random.nextLong());
        playerData.putBoolean(REFRESHED_KEY, true);
        blockEntity.setChanged();

        lootBlockEntity.getOpeners().remove(player.getUUID());
        lootBlockEntity.updatePacketViaState();
        TreasureHuntersGlovesEffects.playRefreshEffects(level, List.of(blockEntity.getBlockPos()));
    }

    private static ILootrInventory getPlayerInventory(ServerLevel level, ServerPlayer player,
                                                      ILootBlockEntity lootBlockEntity) {
        ChestData chestData = DataStorage.getEntityData(
                level,
                lootBlockEntity.getPosition(),
                lootBlockEntity.getTileId()
        );
        return chestData.getInventory(player);
    }

    private static CompoundTag getPlayerData(BlockEntity blockEntity, UUID playerId) {
        return blockEntity.getPersistentData()
                .getCompound(BLOCK_DATA_TAG)
                .getCompound(PLAYERS_KEY)
                .getCompound(playerId.toString());
    }

    private static CompoundTag getOrCreatePlayerData(BlockEntity blockEntity, UUID playerId) {
        CompoundTag persistentData = blockEntity.getPersistentData();
        if (!persistentData.contains(BLOCK_DATA_TAG, Tag.TAG_COMPOUND)) {
            persistentData.put(BLOCK_DATA_TAG, new CompoundTag());
        }

        CompoundTag blockData = persistentData.getCompound(BLOCK_DATA_TAG);
        if (!blockData.contains(PLAYERS_KEY, Tag.TAG_COMPOUND)) {
            blockData.put(PLAYERS_KEY, new CompoundTag());
        }

        CompoundTag players = blockData.getCompound(PLAYERS_KEY);
        String playerKey = playerId.toString();
        if (!players.contains(playerKey, Tag.TAG_COMPOUND)) {
            players.put(playerKey, new CompoundTag());
        }
        return players.getCompound(playerKey);
    }

    private record PendingOpen(ResourceKey<Level> dimension, BlockPos position, UUID containerId, long gameTime) {
    }
}

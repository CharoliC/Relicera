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
import net.minecraft.world.entity.Entity;
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
import noobanidus.mods.lootr.entity.LootrChestMinecartEntity;
import noobanidus.mods.lootr.util.ChestUtil;

public final class LootrTreasureHuntersGlovesCompat {
    private static final String BLOCK_DATA_TAG = "ReliceraTreasureHuntersGlovesLootr";
    private static final String PLAYERS_KEY = "Players";
    private static final String INITIAL_CONTENTS_KEY = "InitialContents";
    private static final String REFRESHED_KEY = "Refreshed";
    private static final long PENDING_OPEN_TIMEOUT_TICKS = 5L;
    private static final Map<UUID, PendingOpen> PENDING_FIRST_OPENS = new HashMap<>();
    private static final Map<UUID, PendingCartOpen> PENDING_FIRST_CART_OPENS = new HashMap<>();

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

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getEntity() instanceof ServerPlayer player)
                || !(player.level() instanceof ServerLevel level)
                || event.getHand() != InteractionHand.MAIN_HAND
                || !(event.getTarget() instanceof LootrChestMinecartEntity minecart)) {
            return;
        }

        if (player.isShiftKeyDown()
                && player.getMainHandItem().isEmpty()
                && player.getOffhandItem().isEmpty()
                && TreasureHuntersGlovesEffects.isEquipped(player)) {
            tryRefreshCart(level, player, minecart);
            event.setCancellationResult(InteractionResult.CONSUME);
            event.setCanceled(true);
            return;
        }

        if (!player.isShiftKeyDown()
                && TreasureHuntersGlovesEffects.isEquipped(player)
                && minecart.getLootTable() != null
                && !DataStorage.isScored(player.getUUID(), minecart.getUUID())) {
            PENDING_FIRST_CART_OPENS.put(player.getUUID(), new PendingCartOpen(
                    level.dimension(),
                    minecart.getUUID(),
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

        recordFirstBlockOpen(level, player);
        recordFirstCartOpen(level, player);
    }

    private static void recordFirstBlockOpen(ServerLevel level, ServerPlayer player) {
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

    private static void recordFirstCartOpen(ServerLevel level, ServerPlayer player) {
        PendingCartOpen pending = PENDING_FIRST_CART_OPENS.remove(player.getUUID());
        if (pending == null
                || !pending.dimension().equals(level.dimension())
                || level.getGameTime() - pending.gameTime() > PENDING_OPEN_TIMEOUT_TICKS) {
            return;
        }

        Entity entity = level.getEntity(pending.minecartId());
        if (!(entity instanceof LootrChestMinecartEntity minecart)) {
            return;
        }

        ILootrInventory inventory = getPlayerInventory(level, player, minecart);
        if (inventory == null
                || !TreasureHuntersGlovesEffects.isEquipped(player)
                || getPlayerData(minecart.getPersistentData(), player.getUUID())
                .contains(INITIAL_CONTENTS_KEY, Tag.TAG_LIST)) {
            return;
        }

        CompoundTag playerData = getOrCreatePlayerData(minecart.getPersistentData(), player.getUUID());
        playerData.put(
                INITIAL_CONTENTS_KEY,
                TreasureHuntersGlovesContainerContents.createSignature(inventory)
        );
        playerData.putBoolean(REFRESHED_KEY, false);
        TreasureHuntersGlovesEffects.recordFirstOpenedLootContainer(player);
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        PENDING_FIRST_OPENS.remove(event.getEntity().getUUID());
        PENDING_FIRST_CART_OPENS.remove(event.getEntity().getUUID());
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

        ChestUtil.handleLootSneak(blockEntity.getBlockState().getBlock(), level, blockEntity.getBlockPos(), player);
        TreasureHuntersGlovesEffects.playRefreshEffects(level, List.of(blockEntity.getBlockPos()));
    }

    private static void tryRefreshCart(ServerLevel level, ServerPlayer player,
                                       LootrChestMinecartEntity minecart) {
        CompoundTag playerData = getPlayerData(minecart.getPersistentData(), player.getUUID());
        ResourceLocation lootTableId = minecart.getLootTable();
        if (lootTableId == null
                || !playerData.contains(INITIAL_CONTENTS_KEY, Tag.TAG_LIST)
                || playerData.getBoolean(REFRESHED_KEY)) {
            return;
        }

        ILootrInventory inventory = getPlayerInventory(level, player, minecart);
        if (inventory == null
                || !TreasureHuntersGlovesContainerContents.canRefresh(
                List.of(playerData.getList(INITIAL_CONTENTS_KEY, Tag.TAG_COMPOUND)),
                List.of(inventory),
                ModCommonConfig.TREASURE_HUNTERS_GLOVES_MAX_ITEMS_TAKEN_BEFORE_REFRESH.get()
        )) {
            return;
        }

        inventory.clearContent();
        minecart.addLoot(player, inventory, lootTableId, level.random.nextLong());
        playerData.putBoolean(REFRESHED_KEY, true);

        ChestUtil.handleLootCartSneak(level, minecart, player);
        TreasureHuntersGlovesEffects.playRefreshEffects(
                level,
                minecart.position().add(0.0D, 0.5D, 0.0D)
        );
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

    private static ILootrInventory getPlayerInventory(ServerLevel level, ServerPlayer player,
                                                      LootrChestMinecartEntity minecart) {
        ChestData chestData = DataStorage.getEntityData(
                level,
                minecart.blockPosition(),
                minecart.getUUID()
        );
        return chestData.getInventory(player);
    }

    private static CompoundTag getPlayerData(BlockEntity blockEntity, UUID playerId) {
        return getPlayerData(blockEntity.getPersistentData(), playerId);
    }

    private static CompoundTag getPlayerData(CompoundTag persistentData, UUID playerId) {
        return persistentData
                .getCompound(BLOCK_DATA_TAG)
                .getCompound(PLAYERS_KEY)
                .getCompound(playerId.toString());
    }

    private static CompoundTag getOrCreatePlayerData(BlockEntity blockEntity, UUID playerId) {
        return getOrCreatePlayerData(blockEntity.getPersistentData(), playerId);
    }

    private static CompoundTag getOrCreatePlayerData(CompoundTag persistentData, UUID playerId) {
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

    private record PendingCartOpen(ResourceKey<Level> dimension, UUID minecartId, long gameTime) {
    }
}

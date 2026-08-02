package com.yukari.relicera.common.curio;

import com.yukari.relicera.config.ModCommonConfig;
import com.yukari.relicera.registry.ModItems;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

public final class TreasureHuntersGlovesEffects {
    private static final String OPENED_LOOT_CONTAINERS_TAG = "TreasureHuntersGlovesOpenedLootContainers";
    private static final String BLOCK_DATA_TAG = "ReliceraTreasureHuntersGloves";
    private static final String LOOT_TABLE_KEY = "LootTable";
    private static final String INITIAL_CONTENTS_KEY = "InitialContents";
    private static final String REFRESHED_KEY = "Refreshed";

    private static final UUID LUCK_MODIFIER_ID = UUID.fromString("809da44f-7c5d-4cfb-bd77-72ad471d6d82");
    private static final String LUCK_MODIFIER_NAME = "Relicera treasure hunter's gloves luck";
    private static final Map<UUID, DoubleChestOpenKey> LAST_DOUBLE_CHEST_OPEN_BY_PLAYER = new HashMap<>();

    private TreasureHuntersGlovesEffects() {
    }

    public static boolean isEquipped(Player player) {
        return CuriosApi.getCuriosInventory(player)
                .resolve()
                .map(handler -> handler.isEquipped(ModItems.TREASURE_HUNTERS_GLOVES.get()))
                .orElse(false);
    }

    public static int getOpenedLootContainerCount(ItemStack stack) {
        if (stack.isEmpty() || !stack.is(ModItems.TREASURE_HUNTERS_GLOVES.get()) || !stack.hasTag()) {
            return 0;
        }
        return stack.getTag().getInt(OPENED_LOOT_CONTAINERS_TAG);
    }

    public static double getLuckBonus(ItemStack stack) {
        return getOpenedLootContainerCount(stack) * ModCommonConfig.TREASURE_HUNTERS_GLOVES_LUCK_PER_CHEST.get();
    }

    public static void recordFirstOpenedLootContainer(ServerPlayer player) {
        if (isEquipped(player)) {
            incrementEquippedGloves(player);
        }
    }

    public static void onLootTableGenerated(RandomizableContainerBlockEntity container, Player player,
                                            ResourceLocation lootTableId) {
        if (!(player instanceof ServerPlayer serverPlayer)
                || !(container.getLevel() instanceof ServerLevel serverLevel)
                || lootTableId == null
                || !isEquipped(serverPlayer)) {
            return;
        }

        rememberRefreshData(container, lootTableId);
        if (shouldIncrementLuck(container, serverPlayer, serverLevel)) {
            incrementEquippedGloves(serverPlayer);
        }
    }

    public static void tickPlayerLuck(Player player) {
        if (player.level().isClientSide()) {
            return;
        }

        AttributeInstance luck = player.getAttribute(Attributes.LUCK);
        if (luck == null) {
            return;
        }

        double amount = getEquippedGloves(player).stream()
                .mapToDouble(TreasureHuntersGlovesEffects::getLuckBonus)
                .sum();
        AttributeModifier existing = luck.getModifier(LUCK_MODIFIER_ID);
        if (amount <= 0.0D) {
            if (existing != null) {
                luck.removeModifier(LUCK_MODIFIER_ID);
            }
            return;
        }

        if (existing != null && Math.abs(existing.getAmount() - amount) < 0.0001D) {
            return;
        }

        if (existing != null) {
            luck.removeModifier(LUCK_MODIFIER_ID);
        }
        luck.addTransientModifier(new AttributeModifier(
                LUCK_MODIFIER_ID,
                LUCK_MODIFIER_NAME,
                amount,
                AttributeModifier.Operation.ADDITION
        ));
    }

    public static void refreshLootContainer(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer player)
                || !(player.level() instanceof ServerLevel level)
                || event.getHand() != InteractionHand.MAIN_HAND
                || !player.isShiftKeyDown()
                || !player.getMainHandItem().isEmpty()
                || !player.getOffhandItem().isEmpty()
                || !isEquipped(player)) {
            return;
        }

        List<RandomizableContainerBlockEntity> containers = getRefreshTargets(level, event.getPos());
        if (containers.isEmpty() || !canRefreshAll(containers)) {
            return;
        }

        refreshAll(level, player, containers);
        playRefreshEffects(level, containers.stream()
                .map(RandomizableContainerBlockEntity::getBlockPos)
                .toList());
        event.setCancellationResult(InteractionResult.CONSUME);
        event.setCanceled(true);
    }

    private static void rememberRefreshData(RandomizableContainerBlockEntity container, ResourceLocation lootTableId) {
        CompoundTag data = getOrCreateRefreshData(container);
        data.putString(LOOT_TABLE_KEY, lootTableId.toString());
        data.put(INITIAL_CONTENTS_KEY, TreasureHuntersGlovesContainerContents.createSignature(container));
        data.putBoolean(REFRESHED_KEY, false);
        container.setChanged();
    }

    private static boolean shouldIncrementLuck(RandomizableContainerBlockEntity container, ServerPlayer player,
                                               ServerLevel level) {
        Optional<DoubleChestOpenKey> doubleChestKey = getDoubleChestOpenKey(container, level);
        if (doubleChestKey.isEmpty()) {
            return true;
        }

        DoubleChestOpenKey key = doubleChestKey.get();
        if (key.equals(LAST_DOUBLE_CHEST_OPEN_BY_PLAYER.get(player.getUUID()))) {
            return false;
        }

        LAST_DOUBLE_CHEST_OPEN_BY_PLAYER.put(player.getUUID(), key);
        return true;
    }

    private static Optional<DoubleChestOpenKey> getDoubleChestOpenKey(RandomizableContainerBlockEntity container,
                                                                      ServerLevel level) {
        if (!(container instanceof ChestBlockEntity)) {
            return Optional.empty();
        }

        BlockState state = container.getBlockState();
        if (!(state.getBlock() instanceof ChestBlock) || state.getValue(ChestBlock.TYPE) == ChestType.SINGLE) {
            return Optional.empty();
        }

        BlockPos pos = container.getBlockPos();
        BlockPos partnerPos = pos.relative(ChestBlock.getConnectedDirection(state));
        BlockPos first = comparePositions(pos, partnerPos) <= 0 ? pos : partnerPos;
        BlockPos second = comparePositions(pos, partnerPos) <= 0 ? partnerPos : pos;
        return Optional.of(new DoubleChestOpenKey(level.dimension(), first.immutable(), second.immutable(), level.getGameTime()));
    }

    private static void incrementEquippedGloves(ServerPlayer player) {
        getEquippedGloves(player).stream()
                .findFirst()
                .ifPresent(stack -> stack.getOrCreateTag().putInt(
                        OPENED_LOOT_CONTAINERS_TAG,
                        getOpenedLootContainerCount(stack) + 1
                ));
    }

    private static List<ItemStack> getEquippedGloves(Player player) {
        return CuriosApi.getCuriosInventory(player)
                .resolve()
                .map(handler -> handler.findCurios(ModItems.TREASURE_HUNTERS_GLOVES.get()).stream()
                        .map(SlotResult::stack)
                        .toList())
                .orElse(List.of());
    }

    private static List<RandomizableContainerBlockEntity> getRefreshTargets(ServerLevel level, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof RandomizableContainerBlockEntity container)) {
            return List.of();
        }

        List<RandomizableContainerBlockEntity> containers = new ArrayList<>();
        containers.add(container);

        BlockState state = container.getBlockState();
        if (container instanceof ChestBlockEntity
                && state.getBlock() instanceof ChestBlock
                && state.getValue(ChestBlock.TYPE) != ChestType.SINGLE) {
            BlockPos partnerPos = pos.relative(ChestBlock.getConnectedDirection(state));
            BlockEntity partner = level.getBlockEntity(partnerPos);
            if (partner instanceof RandomizableContainerBlockEntity partnerContainer
                    && partnerContainer instanceof ChestBlockEntity
                    && partnerContainer != container) {
                containers.add(partnerContainer);
            }
        }

        containers.sort(Comparator.comparing(RandomizableContainerBlockEntity::getBlockPos, TreasureHuntersGlovesEffects::comparePositions));
        return containers;
    }

    private static boolean canRefreshAll(List<RandomizableContainerBlockEntity> containers) {
        List<ListTag> initialSignatures = new ArrayList<>();
        for (RandomizableContainerBlockEntity container : containers) {
            CompoundTag data = getRefreshData(container);
            if (!data.contains(LOOT_TABLE_KEY, Tag.TAG_STRING)
                    || !data.contains(INITIAL_CONTENTS_KEY, Tag.TAG_LIST)
                    || data.getBoolean(REFRESHED_KEY)) {
                return false;
            }

            initialSignatures.add(data.getList(INITIAL_CONTENTS_KEY, Tag.TAG_COMPOUND));
        }
        return TreasureHuntersGlovesContainerContents.canRefresh(
                initialSignatures,
                containers,
                ModCommonConfig.TREASURE_HUNTERS_GLOVES_MAX_ITEMS_TAKEN_BEFORE_REFRESH.get()
        );
    }

    private static void refreshAll(ServerLevel level, ServerPlayer player, List<RandomizableContainerBlockEntity> containers) {
        List<RefreshTarget> targets = containers.stream()
                .map(container -> new RefreshTarget(container, ResourceLocation.parse(getRefreshData(container).getString(LOOT_TABLE_KEY))))
                .toList();

        for (RefreshTarget target : targets) {
            target.container().clearContent();
        }

        for (RefreshTarget target : targets) {
            fillFromLootTable(level, player, target.container(), target.lootTableId());
            markRefreshed(level, target.container());
        }
    }

    private static void fillFromLootTable(ServerLevel level, ServerPlayer player,
                                          RandomizableContainerBlockEntity container, ResourceLocation lootTableId) {
        LootTable lootTable = level.getServer().getLootData().getLootTable(lootTableId);
        LootParams.Builder lootParams = new LootParams.Builder(level)
                .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(container.getBlockPos()))
                .withLuck(player.getLuck())
                .withParameter(LootContextParams.THIS_ENTITY, player);
        lootTable.fill(container, lootParams.create(LootContextParamSets.CHEST), level.random.nextLong());
    }

    private static void markRefreshed(ServerLevel level, RandomizableContainerBlockEntity container) {
        getOrCreateRefreshData(container).putBoolean(REFRESHED_KEY, true);
        container.setChanged();

        BlockPos pos = container.getBlockPos();
        BlockState state = container.getBlockState();
        level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        level.updateNeighbourForOutputSignal(pos, state.getBlock());
    }

    public static void playRefreshEffects(ServerLevel level, List<BlockPos> positions) {
        int minX = positions.stream().mapToInt(BlockPos::getX).min().orElse(0);
        int maxX = positions.stream().mapToInt(BlockPos::getX).max().orElse(minX);
        int minY = positions.stream().mapToInt(BlockPos::getY).min().orElse(0);
        int maxY = positions.stream().mapToInt(BlockPos::getY).max().orElse(minY);
        int minZ = positions.stream().mapToInt(BlockPos::getZ).min().orElse(0);
        int maxZ = positions.stream().mapToInt(BlockPos::getZ).max().orElse(minZ);

        double centerX = (minX + maxX + 1.0D) * 0.5D;
        double centerY = (minY + maxY + 1.0D) * 0.5D;
        double centerZ = (minZ + maxZ + 1.0D) * 0.5D;
        double spreadX = (maxX - minX + 1.0D) * 0.4D;
        double spreadZ = (maxZ - minZ + 1.0D) * 0.4D;
        int particleCount = positions.size() == 1 ? 32 : 48;

        level.sendParticles(
                ParticleTypes.REVERSE_PORTAL,
                centerX,
                centerY,
                centerZ,
                particleCount,
                spreadX,
                0.4D,
                spreadZ,
                0.08D
        );
        level.playSound(
                null,
                centerX,
                centerY,
                centerZ,
                SoundEvents.RESPAWN_ANCHOR_CHARGE,
                SoundSource.BLOCKS,
                1.0F,
                1.0F
        );
    }

    private static CompoundTag getRefreshData(RandomizableContainerBlockEntity container) {
        return container.getPersistentData().getCompound(BLOCK_DATA_TAG);
    }

    private static CompoundTag getOrCreateRefreshData(RandomizableContainerBlockEntity container) {
        CompoundTag persistentData = container.getPersistentData();
        if (!persistentData.contains(BLOCK_DATA_TAG, Tag.TAG_COMPOUND)) {
            persistentData.put(BLOCK_DATA_TAG, new CompoundTag());
        }
        return persistentData.getCompound(BLOCK_DATA_TAG);
    }

    private static int comparePositions(BlockPos first, BlockPos second) {
        if (first.getX() != second.getX()) {
            return Integer.compare(first.getX(), second.getX());
        }
        if (first.getY() != second.getY()) {
            return Integer.compare(first.getY(), second.getY());
        }
        return Integer.compare(first.getZ(), second.getZ());
    }

    private record DoubleChestOpenKey(ResourceKey<Level> dimension, BlockPos first, BlockPos second, long gameTime) {
    }

    private record RefreshTarget(RandomizableContainerBlockEntity container, ResourceLocation lootTableId) {
    }

}

package com.yukari.relicera.common.block;

import com.yukari.relicera.ReliceraMod;
import com.yukari.relicera.config.ModCommonConfig;
import com.yukari.relicera.mixin.PlayerAccessor;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.level.SleepFinishedTimeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class DreamcatcherBoxSleepRewards {
    private static final ResourceLocation NIGHTMARE_LOOT = ResourceLocation.fromNamespaceAndPath(ReliceraMod.MOD_ID, "gameplay/dreamcatcher_box/nightmare");
    private static final ResourceLocation DREAM_LOOT = ResourceLocation.fromNamespaceAndPath(ReliceraMod.MOD_ID, "gameplay/dreamcatcher_box/dream");
    private static final ResourceLocation VILLAGER_LOOT = ResourceLocation.fromNamespaceAndPath(ReliceraMod.MOD_ID, "gameplay/dreamcatcher_box/villager");
    private static final ResourceLocation MAID_LOOT = ResourceLocation.fromNamespaceAndPath(ReliceraMod.MOD_ID, "gameplay/dreamcatcher_box/maid");
    private static final ResourceLocation TOUHOU_MAID_ENTITY = ResourceLocation.fromNamespaceAndPath("touhou_little_maid", "maid");
    private static final ResourceLocation ENIGMATIC_CURSED_RING = ResourceLocation.fromNamespaceAndPath("enigmaticlegacy", "cursed_ring");
    private static final String ENIGMATIC_LEGACY_MOD_ID = "enigmaticlegacy";
    private static final String CURSED_SLEEP_MESSAGE = "message.relicera.dreamcatcher_box.cursed_sleep";
    private static final int VILLAGER_SCAN_INTERVAL_TICKS = 200;
    private static final int TOUHOU_MAID_SCAN_INTERVAL_TICKS = 40;
    private static final long TOUHOU_MAID_REWARD_COOLDOWN_TICKS = 12000L;
    private static final int ENIGMATIC_SLEEP_TIMER_LIMIT = 90;
    private static final int VANILLA_SLEEP_TIMER_READY = 100;

    private static final Map<ResourceKey<Level>, LevelState> LEVEL_STATES = new HashMap<>();

    private DreamcatcherBoxSleepRewards() {
    }

    public static void onSleepFinished(SleepFinishedTimeEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }

        LevelState state = getState(level);
        for (ServerPlayer player : level.players()) {
            Optional<BlockPos> sleepingPos = player.getSleepingPos();
            if (player.isSleeping() && sleepingPos.isPresent()) {
                rewardPlayer(level, state, player, sleepingPos.get());
            }
        }
        recordSleepingTouhouMaids(level, state);
        rewardRecordedTouhouMaids(level, state);
    }

    public static void tickLevel(ServerLevel level) {
        LevelState state = getState(level);
        boolean isNight = level.isNight();

        if (level.getGameTime() % TOUHOU_MAID_SCAN_INTERVAL_TICKS == 0L) {
            tickTouhouMaidSessions(level, state);
        }

        if (isNight) {
            if (!state.wasNight) {
                state.startNight();
            }
            if (level.getGameTime() % VILLAGER_SCAN_INTERVAL_TICKS == 0L) {
                recordSleepingVillagers(level, state);
            }
            state.wasNight = true;
            return;
        }

        if (state.wasNight) {
            rewardRecordedVillagers(level, state);
            state.endNight();
        }
    }

    public static void allowEnigmaticCursedSleep(ServerPlayer player) {
        if (!ModList.get().isLoaded(ENIGMATIC_LEGACY_MOD_ID) || !(player.level() instanceof ServerLevel level)) {
            return;
        }

        LevelState state = getState(level);
        UUID uuid = player.getUUID();
        if (!player.isSleeping()) {
            state.cursedSleepMessagePlayers.remove(uuid);
            return;
        }

        if (player.getSleepTimer() != ENIGMATIC_SLEEP_TIMER_LIMIT || !hasEnigmaticCursedRing(player)) {
            return;
        }

        Optional<BlockPos> sleepingPos = player.getSleepingPos();
        if (sleepingPos.isEmpty()) {
            return;
        }

        double range = ModCommonConfig.DREAMCATCHER_BOX_SLEEP_RANGE.get();
        if (range > 0.0D && hasNearbyDreamcatcherBox(level, sleepingPos.get(), range)) {
            ((PlayerAccessor) player).relicera$setSleepCounter(VANILLA_SLEEP_TIMER_READY);
            if (state.cursedSleepMessagePlayers.add(uuid)) {
                player.sendSystemMessage(Component.translatable(CURSED_SLEEP_MESSAGE).withStyle(ChatFormatting.LIGHT_PURPLE));
            }
        }
    }

    private static void rewardPlayer(ServerLevel level, LevelState state, ServerPlayer player, BlockPos bedPos) {
        UUID uuid = player.getUUID();
        if (!state.rewardedEntities.add(uuid)) {
            return;
        }

        ResourceLocation lootTable = level.random.nextBoolean() ? DREAM_LOOT : NIGHTMARE_LOOT;
        rewardAt(level, bedPos, lootTable);
    }

    private static void recordSleepingVillagers(ServerLevel level, LevelState state) {
        double range = ModCommonConfig.DREAMCATCHER_BOX_SLEEP_RANGE.get();
        if (range <= 0.0D) {
            return;
        }

        for (Entity entity : level.getAllEntities()) {
            if (!(entity instanceof Villager villager)
                    || !villager.isAlive()
                    || !villager.isSleeping()
                    || state.rewardedEntities.contains(villager.getUUID())
                    || state.sleepingVillagers.containsKey(villager.getUUID())) {
                continue;
            }

            villager.getSleepingPos()
                    .filter(bedPos -> hasNearbyDreamcatcherBox(level, bedPos, range))
                    .ifPresent(bedPos -> state.sleepingVillagers.put(villager.getUUID(), bedPos.immutable()));
        }
    }

    private static void rewardRecordedVillagers(ServerLevel level, LevelState state) {
        for (Map.Entry<UUID, BlockPos> entry : state.sleepingVillagers.entrySet()) {
            if (state.rewardedEntities.add(entry.getKey())) {
                rewardAt(level, entry.getValue(), VILLAGER_LOOT);
            }
        }
    }

    private static void recordSleepingTouhouMaids(ServerLevel level, LevelState state) {
        double range = ModCommonConfig.DREAMCATCHER_BOX_SLEEP_RANGE.get();
        if (range <= 0.0D) {
            return;
        }

        long gameTime = level.getGameTime();
        cleanupTouhouMaidCooldowns(state, gameTime);
        for (Entity entity : level.getAllEntities()) {
            if (!(entity instanceof LivingEntity living)
                    || !isTouhouMaid(living)
                    || !living.isAlive()
                    || !living.isSleeping()
                    || isTouhouMaidOnCooldown(state, living.getUUID(), gameTime)
                    || state.sleepingTouhouMaids.containsKey(living.getUUID())) {
                continue;
            }

            living.getSleepingPos()
                    .filter(bedPos -> hasNearbyDreamcatcherBox(level, bedPos, range))
                    .ifPresent(bedPos -> state.sleepingTouhouMaids.put(living.getUUID(), bedPos.immutable()));
        }
    }

    private static void rewardRecordedTouhouMaids(ServerLevel level, LevelState state) {
        for (Map.Entry<UUID, BlockPos> entry : state.sleepingTouhouMaids.entrySet()) {
            rewardTouhouMaid(level, state, entry.getKey(), entry.getValue());
        }
        state.sleepingTouhouMaids.clear();
    }

    private static void tickTouhouMaidSessions(ServerLevel level, LevelState state) {
        recordSleepingTouhouMaids(level, state);
        rewardAwakeTouhouMaids(level, state);
    }

    private static void rewardAwakeTouhouMaids(ServerLevel level, LevelState state) {
        state.sleepingTouhouMaids.entrySet().removeIf(entry -> {
            Entity entity = level.getEntity(entry.getKey());
            if (!(entity instanceof LivingEntity living) || !isTouhouMaid(living)) {
                return false;
            }
            if (living.isSleeping()) {
                return false;
            }

            rewardTouhouMaid(level, state, entry.getKey(), entry.getValue());
            return true;
        });
    }

    private static void rewardTouhouMaid(ServerLevel level, LevelState state, UUID uuid, BlockPos bedPos) {
        long gameTime = level.getGameTime();
        if (isTouhouMaidOnCooldown(state, uuid, gameTime)) {
            return;
        }
        if (rewardAt(level, bedPos, MAID_LOOT)) {
            state.touhouMaidCooldowns.put(uuid, gameTime + TOUHOU_MAID_REWARD_COOLDOWN_TICKS);
        }
    }

    private static boolean isTouhouMaidOnCooldown(LevelState state, UUID uuid, long gameTime) {
        Long cooldownEnd = state.touhouMaidCooldowns.get(uuid);
        return cooldownEnd != null && cooldownEnd > gameTime;
    }

    private static void cleanupTouhouMaidCooldowns(LevelState state, long gameTime) {
        state.touhouMaidCooldowns.entrySet().removeIf(entry -> entry.getValue() <= gameTime);
    }

    private static boolean isTouhouMaid(LivingEntity entity) {
        ResourceLocation entityId = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());
        return TOUHOU_MAID_ENTITY.equals(entityId);
    }

    private static boolean hasEnigmaticCursedRing(ServerPlayer player) {
        Item cursedRing = ForgeRegistries.ITEMS.getValue(ENIGMATIC_CURSED_RING);
        if (cursedRing == null) {
            return false;
        }

        return CuriosApi.getCuriosInventory(player)
                .resolve()
                .map(handler -> handler.isEquipped(cursedRing))
                .orElse(false);
    }

    private static boolean rewardAt(ServerLevel level, BlockPos origin, ResourceLocation lootTableId) {
        List<ItemStack> loot = generateLoot(level, origin, lootTableId).stream()
                .filter(stack -> !stack.isEmpty())
                .toList();
        if (loot.isEmpty()) {
            return false;
        }

        DreamcatcherBoxBlockEntity box = findNearestAcceptingBox(level, origin, ModCommonConfig.DREAMCATCHER_BOX_SLEEP_RANGE.get(), loot);
        return box != null && box.insertAll(loot);
    }

    private static List<ItemStack> generateLoot(ServerLevel level, BlockPos origin, ResourceLocation lootTableId) {
        LootTable lootTable = level.getServer().getLootData().getLootTable(lootTableId);
        LootParams lootParams = new LootParams.Builder(level)
                .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(origin))
                .create(LootContextParamSets.CHEST);
        List<ItemStack> loot = new ArrayList<>();
        getRandomItemsWithoutGlobalLootModifiers(lootTable, lootParams, loot::add);
        return loot;
    }

    @SuppressWarnings("deprecation")
    private static void getRandomItemsWithoutGlobalLootModifiers(LootTable lootTable, LootParams lootParams, java.util.function.Consumer<ItemStack> output) {
        lootTable.getRandomItemsRaw(lootParams, output);
    }

    private static boolean hasNearbyDreamcatcherBox(ServerLevel level, BlockPos origin, double range) {
        return findNearestBox(level, origin, range, null) != null;
    }

    private static DreamcatcherBoxBlockEntity findNearestAcceptingBox(ServerLevel level, BlockPos origin, double range, List<ItemStack> loot) {
        return findNearestBox(level, origin, range, loot);
    }

    private static DreamcatcherBoxBlockEntity findNearestBox(ServerLevel level, BlockPos origin, double range, List<ItemStack> requiredSpaceFor) {
        if (range <= 0.0D) {
            return null;
        }

        int blockRange = (int) Math.ceil(range);
        double maxDistance = range * range;
        Vec3 originCenter = Vec3.atCenterOf(origin);
        DreamcatcherBoxBlockEntity nearest = null;
        double nearestDistance = Double.MAX_VALUE;

        BlockPos min = origin.offset(-blockRange, -blockRange, -blockRange);
        BlockPos max = origin.offset(blockRange, blockRange, blockRange);
        for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
            double distance = Vec3.atCenterOf(pos).distanceToSqr(originCenter);
            if (distance > maxDistance || distance >= nearestDistance || !level.isLoaded(pos)) {
                continue;
            }

            if (level.getBlockEntity(pos) instanceof DreamcatcherBoxBlockEntity box
                    && (requiredSpaceFor == null || box.canInsertAll(requiredSpaceFor))) {
                nearest = box;
                nearestDistance = distance;
            }
        }

        return nearest;
    }

    private static LevelState getState(ServerLevel level) {
        return LEVEL_STATES.computeIfAbsent(level.dimension(), key -> new LevelState());
    }

    private static final class LevelState {
        private final Map<UUID, BlockPos> sleepingVillagers = new HashMap<>();
        private final Map<UUID, BlockPos> sleepingTouhouMaids = new HashMap<>();
        private final Map<UUID, Long> touhouMaidCooldowns = new HashMap<>();
        private final java.util.Set<UUID> rewardedEntities = new java.util.HashSet<>();
        private final java.util.Set<UUID> cursedSleepMessagePlayers = new java.util.HashSet<>();
        private boolean wasNight;

        private void startNight() {
            sleepingVillagers.clear();
            rewardedEntities.clear();
        }

        private void endNight() {
            sleepingVillagers.clear();
            wasNight = false;
        }
    }
}

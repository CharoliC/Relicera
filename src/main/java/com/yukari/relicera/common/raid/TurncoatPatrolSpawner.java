package com.yukari.relicera.common.raid;

import com.yukari.relicera.common.curio.TurncoatsMedalEffects;
import com.yukari.relicera.config.ModCommonConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.monster.PatrollingMonster;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class TurncoatPatrolSpawner {
    private static final int GLOBAL_SPAWN_THROTTLE_TICKS = 20 * 60;
    private static final int NEARBY_PATROL_RADIUS = 96;
    private static final int MINIMUM_WORLD_DAY = 5;
    private static final int MINIMUM_SPAWN_DISTANCE = 24;
    private static final int SPAWN_DISTANCE_VARIATION = 24;
    private static final int SPAWN_POSITION_ATTEMPTS = 12;
    private static final Map<UUID, Long> NEXT_ATTEMPT_TICKS = new HashMap<>();
    private static final Map<ServerLevel, Long> LAST_LEVEL_SPAWN_TICKS = new HashMap<>();

    private TurncoatPatrolSpawner() {
    }

    public static void tickLevel(ServerLevel level) {
        int baseInterval = ModCommonConfig.TURNCOATS_MEDAL_PATROL_ATTEMPT_INTERVAL_TICKS.get();
        if (baseInterval <= 0 || level.getGameTime() % 20L != 0L) {
            return;
        }

        long gameTime = level.getGameTime();
        for (ServerPlayer player : level.players()) {
            if (!player.isAlive() || player.isSpectator() || !TurncoatsMedalEffects.isEquipped(player)) {
                continue;
            }

            long nextAttempt = NEXT_ATTEMPT_TICKS.computeIfAbsent(
                    player.getUUID(),
                    ignored -> gameTime + getNextInterval(level.random, baseInterval)
            );
            if (gameTime < nextAttempt) {
                continue;
            }

            long lastLevelSpawn = LAST_LEVEL_SPAWN_TICKS.getOrDefault(
                    level,
                    gameTime - GLOBAL_SPAWN_THROTTLE_TICKS
            );
            if (gameTime - lastLevelSpawn < GLOBAL_SPAWN_THROTTLE_TICKS) {
                continue;
            }

            NEXT_ATTEMPT_TICKS.put(player.getUUID(), gameTime + getNextInterval(level.random, baseInterval));
            if (trySpawnPatrol(level, player)) {
                LAST_LEVEL_SPAWN_TICKS.put(level, gameTime);
            }
        }
    }

    public static void forgetPlayer(ServerPlayer player) {
        NEXT_ATTEMPT_TICKS.remove(player.getUUID());
    }

    public static void clear() {
        NEXT_ATTEMPT_TICKS.clear();
        LAST_LEVEL_SPAWN_TICKS.clear();
    }

    private static long getNextInterval(RandomSource random, int baseInterval) {
        int variationBound = Math.max(1, baseInterval / 10 + 1);
        return (long) baseInterval + random.nextInt(variationBound);
    }

    private static boolean trySpawnPatrol(ServerLevel level, ServerPlayer player) {
        if (!canAttemptPatrol(level, player) || hasNearbyPatrol(level, player)) {
            return false;
        }

        int badOmenLevel = getBadOmenLevel(player);
        BlockPos spawnOrigin = findSpawnOrigin(level, player, badOmenLevel >= 5);
        if (spawnOrigin == null) {
            return false;
        }

        int patrolSize = (int) Math.ceil(level.getCurrentDifficultyAt(spawnOrigin).getEffectiveDifficulty()) + 1;
        if (badOmenLevel >= 3) {
            patrolSize = Math.max(patrolSize, 3);
        }

        List<EntityType<? extends PatrollingMonster>> memberTypes = createMemberTypes(patrolSize, badOmenLevel);
        List<PlannedMember> plannedMembers = planMembers(level, spawnOrigin, memberTypes);
        if (plannedMembers.size() != memberTypes.size()) {
            return false;
        }

        return spawnMembers(level, plannedMembers, badOmenLevel >= 5);
    }

    private static boolean canAttemptPatrol(ServerLevel level, ServerPlayer player) {
        return level.getDifficulty() != Difficulty.PEACEFUL
                && level.dimension() == Level.OVERWORLD
                && level.getServer().isSpawningMonsters()
                && level.getGameRules().getBoolean(GameRules.RULE_DO_PATROL_SPAWNING)
                && level.getDayTime() / 24000L >= MINIMUM_WORLD_DAY
                && level.isDay()
                && !level.isCloseToVillage(player.blockPosition(), 2);
    }

    private static boolean hasNearbyPatrol(ServerLevel level, ServerPlayer player) {
        AABB searchArea = player.getBoundingBox().inflate(NEARBY_PATROL_RADIUS);
        return !level.getEntitiesOfClass(
                PatrollingMonster.class,
                searchArea,
                PatrollingMonster::isPatrolLeader
        ).isEmpty();
    }

    private static int getBadOmenLevel(ServerPlayer player) {
        MobEffectInstance badOmen = player.getEffect(MobEffects.BAD_OMEN);
        return badOmen == null ? 0 : badOmen.getAmplifier() + 1;
    }

    private static BlockPos findSpawnOrigin(ServerLevel level, ServerPlayer player, boolean needsRavagerSpace) {
        RandomSource random = level.random;
        for (int attempt = 0; attempt < SPAWN_POSITION_ATTEMPTS; attempt++) {
            int offsetX = randomOffset(random);
            int offsetZ = randomOffset(random);
            BlockPos horizontalPos = player.blockPosition().offset(offsetX, 0, offsetZ);
            if (!level.hasChunksAt(horizontalPos.getX() - 10, horizontalPos.getZ() - 10,
                    horizontalPos.getX() + 10, horizontalPos.getZ() + 10)) {
                continue;
            }

            BlockPos spawnPos = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, horizontalPos);
            if (level.getBiome(spawnPos).is(BiomeTags.WITHOUT_PATROL_SPAWNS)
                    || !isValidPatrolSpawn(level, spawnPos, needsRavagerSpace ? EntityType.RAVAGER : EntityType.PILLAGER)) {
                continue;
            }
            return spawnPos;
        }
        return null;
    }

    private static int randomOffset(RandomSource random) {
        int distance = MINIMUM_SPAWN_DISTANCE + random.nextInt(SPAWN_DISTANCE_VARIATION);
        return random.nextBoolean() ? distance : -distance;
    }

    private static List<EntityType<? extends PatrollingMonster>> createMemberTypes(int patrolSize, int badOmenLevel) {
        List<EntityType<? extends PatrollingMonster>> types = new ArrayList<>(patrolSize);
        types.add(EntityType.PILLAGER);
        if (badOmenLevel >= 1) {
            types.add(EntityType.VINDICATOR);
        }
        if (badOmenLevel >= 3) {
            types.add(EntityType.EVOKER);
        }
        while (types.size() < patrolSize) {
            types.add(EntityType.PILLAGER);
        }
        return types;
    }

    private static List<PlannedMember> planMembers(
            ServerLevel level,
            BlockPos origin,
            List<EntityType<? extends PatrollingMonster>> memberTypes
    ) {
        List<PlannedMember> plannedMembers = new ArrayList<>(memberTypes.size());
        plannedMembers.add(new PlannedMember(memberTypes.get(0), origin));

        for (int index = 1; index < memberTypes.size(); index++) {
            EntityType<? extends PatrollingMonster> type = memberTypes.get(index);
            BlockPos memberPos = findMemberSpawnPos(level, origin, type);
            if (memberPos == null) {
                return List.of();
            }
            plannedMembers.add(new PlannedMember(type, memberPos));
        }
        return plannedMembers;
    }

    private static BlockPos findMemberSpawnPos(
            ServerLevel level,
            BlockPos origin,
            EntityType<? extends PatrollingMonster> type
    ) {
        RandomSource random = level.random;
        for (int attempt = 0; attempt < SPAWN_POSITION_ATTEMPTS; attempt++) {
            BlockPos horizontalPos = origin.offset(random.nextInt(9) - 4, 0, random.nextInt(9) - 4);
            BlockPos spawnPos = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, horizontalPos);
            if (isValidPatrolSpawn(level, spawnPos, type)) {
                return spawnPos;
            }
        }
        return null;
    }

    private static boolean isValidPatrolSpawn(
            ServerLevel level,
            BlockPos pos,
            EntityType<? extends PatrollingMonster> type
    ) {
        return NaturalSpawner.isValidEmptySpawnBlock(
                level,
                pos,
                level.getBlockState(pos),
                level.getFluidState(pos),
                type
        ) && PatrollingMonster.checkPatrollingMonsterSpawnRules(type, level, MobSpawnType.PATROL, pos, level.random);
    }

    private static boolean spawnMembers(ServerLevel level, List<PlannedMember> plannedMembers, boolean mountCaptain) {
        List<PatrollingMonster> members = new ArrayList<>(plannedMembers.size());
        for (PlannedMember plannedMember : plannedMembers) {
            PatrollingMonster member = plannedMember.type().create(level);
            if (member == null) {
                return false;
            }
            member.moveTo(
                    plannedMember.pos().getX() + 0.5D,
                    plannedMember.pos().getY(),
                    plannedMember.pos().getZ() + 0.5D,
                    level.random.nextFloat() * 360.0F,
                    0.0F
            );
            if (!level.noCollision(member)) {
                return false;
            }
            members.add(member);
        }

        Raider ravager = null;
        if (mountCaptain) {
            ravager = EntityType.RAVAGER.create(level);
            PlannedMember captainPlan = plannedMembers.get(0);
            if (ravager == null) {
                return false;
            }
            ravager.moveTo(
                    captainPlan.pos().getX() + 0.5D,
                    captainPlan.pos().getY(),
                    captainPlan.pos().getZ() + 0.5D,
                    level.random.nextFloat() * 360.0F,
                    0.0F
            );
            if (!level.noCollision(ravager)) {
                return false;
            }
        }

        PatrollingMonster captain = members.get(0);
        captain.setPatrolLeader(true);
        captain.findPatrolTarget();
        finalizePatroller(level, captain);
        for (int index = 1; index < members.size(); index++) {
            PatrollingMonster member = members.get(index);
            member.setPatrolTarget(captain.getPatrolTarget());
            finalizePatroller(level, member);
        }

        if (ravager != null) {
            ravager.setPatrolTarget(captain.getPatrolTarget());
            finalizePatroller(level, ravager);
            captain.startRiding(ravager, true);
            level.addFreshEntityWithPassengers(ravager);
        } else {
            level.addFreshEntity(captain);
        }
        for (int index = 1; index < members.size(); index++) {
            level.addFreshEntity(members.get(index));
        }
        return true;
    }

    private static void finalizePatroller(ServerLevel level, PatrollingMonster patroller) {
        patroller.finalizeSpawn(
                level,
                level.getCurrentDifficultyAt(patroller.blockPosition()),
                MobSpawnType.PATROL,
                (SpawnGroupData) null,
                null
        );
    }

    private record PlannedMember(EntityType<? extends PatrollingMonster> type, BlockPos pos) {
    }
}

package com.yukari.relicera.common.entity.forgeling;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Optional;

public final class ForgelingWorksiteManager {
    private static final int SEARCH_RANGE = 20;
    private static final int WORK_DISTANCE = 3;
    private static final Direction[] WORK_DIRECTIONS = {
            Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST
    };

    private ForgelingWorksiteManager() {
    }

    static Optional<Binding> ensureBinding(Forgeling forgeling) {
        if (!(forgeling.level() instanceof ServerLevel level)) {
            return Optional.empty();
        }

        ForgelingWorksiteData data = ForgelingWorksiteData.get(level);
        BlockPos existingTable = forgeling.getBoundTablePos();
        BlockPos existingWork = forgeling.getBoundWorkPos();
        if (existingTable != null) {
            if (isSmithingTable(level, existingTable) && data.claim(existingTable, forgeling.getUUID())) {
                if (existingWork != null && isValidWorkPosition(level, existingTable, existingWork)) {
                    return Optional.of(new Binding(existingTable, existingWork));
                }

                Optional<BlockPos> replacementWork = findWorkPosition(level, forgeling, existingTable);
                if (replacementWork.isPresent()) {
                    forgeling.setWorksite(existingTable, replacementWork.get());
                    return Optional.of(new Binding(existingTable, replacementWork.get()));
                }
            }

            data.release(existingTable, forgeling.getUUID());
            forgeling.clearWorksite();
        }

        Optional<BlockPos> table = level.getPoiManager().findClosest(
                holder -> holder.is(PoiTypes.TOOLSMITH),
                pos -> isSmithingTable(level, pos)
                        && data.isAvailableTo(pos, forgeling.getUUID())
                        && findWorkPosition(level, forgeling, pos).isPresent(),
                forgeling.blockPosition(),
                SEARCH_RANGE,
                PoiManager.Occupancy.ANY
        );
        if (table.isEmpty()) {
            return Optional.empty();
        }

        Optional<BlockPos> workPosition = findWorkPosition(level, forgeling, table.get());
        if (workPosition.isEmpty() || !data.claim(table.get(), forgeling.getUUID())) {
            return Optional.empty();
        }

        forgeling.setWorksite(table.get(), workPosition.get());
        return Optional.of(new Binding(table.get(), workPosition.get()));
    }

    static boolean validateBinding(Forgeling forgeling, Binding binding) {
        if (!(forgeling.level() instanceof ServerLevel level)) {
            return false;
        }
        return isSmithingTable(level, binding.tablePos)
                && ForgelingWorksiteData.get(level).isOwnedBy(binding.tablePos, forgeling.getUUID())
                && isValidWorkPosition(level, binding.tablePos, binding.workPos);
    }

    static void releaseBinding(Forgeling forgeling) {
        BlockPos tablePos = forgeling.getBoundTablePos();
        if (tablePos != null && forgeling.level() instanceof ServerLevel level) {
            ForgelingWorksiteData.get(level).release(tablePos, forgeling.getUUID());
        }
        forgeling.clearWorksite();
    }

    public static void cleanupInvalidBindings(ServerLevel level) {
        ForgelingWorksiteData.get(level).removeInvalidTables(level);
    }

    static boolean isSmithingTable(ServerLevel level, BlockPos tablePos) {
        return level.getBlockState(tablePos).is(Blocks.SMITHING_TABLE);
    }

    private static Optional<BlockPos> findWorkPosition(ServerLevel level, Forgeling forgeling, BlockPos tablePos) {
        if (!hasTableClearance(level, tablePos)) {
            return Optional.empty();
        }
        return Arrays.stream(WORK_DIRECTIONS)
                .map(direction -> tablePos.relative(direction, WORK_DISTANCE))
                .filter(workPos -> isValidWorkLane(level, tablePos, workPos))
                .min(Comparator.comparingDouble(workPos -> forgeling.distanceToSqr(Vec3.atBottomCenterOf(workPos))));
    }

    private static boolean isValidWorkPosition(ServerLevel level, BlockPos tablePos, BlockPos workPos) {
        return hasTableClearance(level, tablePos) && isValidWorkLane(level, tablePos, workPos);
    }

    private static boolean isValidWorkLane(ServerLevel level, BlockPos tablePos, BlockPos workPos) {
        Direction workDirection = workDirection(tablePos, workPos);
        if (workDirection == null) {
            return false;
        }
        BlockPos floorPos = workPos.below();
        if (!level.getBlockState(floorPos).isFaceSturdy(level, floorPos, Direction.UP)
                || !level.getBlockState(workPos).getCollisionShape(level, workPos).isEmpty()
                || !level.getBlockState(workPos.above()).getCollisionShape(level, workPos.above()).isEmpty()) {
            return false;
        }

        for (int distance = 1; distance < WORK_DISTANCE; distance++) {
            BlockPos lanePos = tablePos.relative(workDirection, distance);
            if (!level.getBlockState(lanePos).getCollisionShape(level, lanePos).isEmpty()
                    || !level.getBlockState(lanePos.above()).getCollisionShape(level, lanePos.above()).isEmpty()
                    || !level.getBlockState(lanePos.above(2)).getCollisionShape(level, lanePos.above(2)).isEmpty()
                    || !level.getBlockState(lanePos.above(3)).getCollisionShape(level, lanePos.above(3)).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Nullable
    private static Direction workDirection(BlockPos tablePos, BlockPos workPos) {
        if (workPos.getY() != tablePos.getY()) {
            return null;
        }
        for (Direction direction : WORK_DIRECTIONS) {
            if (tablePos.relative(direction, WORK_DISTANCE).equals(workPos)) {
                return direction;
            }
        }
        return null;
    }

    private static boolean hasTableClearance(ServerLevel level, BlockPos tablePos) {
        BlockPos first = tablePos.above();
        BlockPos second = tablePos.above(2);
        return level.getBlockState(first).getCollisionShape(level, first).isEmpty()
                && level.getBlockState(second).getCollisionShape(level, second).isEmpty();
    }

    record Binding(BlockPos tablePos, BlockPos workPos) {
    }
}


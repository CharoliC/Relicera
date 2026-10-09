package com.yukari.relicera.common.worldgen.structure;

import com.mojang.serialization.Codec;
import com.yukari.relicera.registry.ModStructures;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

import java.util.Optional;
import java.util.OptionalInt;

public final class AncientForgeStructure extends Structure {
    public static final Codec<AncientForgeStructure> CODEC = simpleCodec(AncientForgeStructure::new);

    private static final int TEMPLATE_HORIZONTAL_CENTER = 16;
    private static final int MIN_STRUCTURE_Y = 24;
    private static final int MAX_STRUCTURE_Y = 88;
    private static final int REQUIRED_CENTER_CLEARANCE = 24;

    public AncientForgeStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        ChunkPos chunkPos = context.chunkPos();
        int centerX = chunkPos.getMiddleBlockX();
        int centerZ = chunkPos.getMiddleBlockZ();
        OptionalInt structureY = findCaveFloor(context, centerX, centerZ);
        if (structureY.isEmpty()) {
            return Optional.empty();
        }

        Rotation rotation = Rotation.getRandom(context.random());
        BlockPos templatePosition = new BlockPos(
                centerX - TEMPLATE_HORIZONTAL_CENTER,
                structureY.getAsInt(),
                centerZ - TEMPLATE_HORIZONTAL_CENTER
        );
        BlockPos generationPoint = new BlockPos(centerX, structureY.getAsInt(), centerZ);
        return Optional.of(new GenerationStub(generationPoint, pieces -> pieces.addPiece(
                new AncientForgePiece(context.structureTemplateManager(), templatePosition, rotation)
        )));
    }

    private static OptionalInt findCaveFloor(GenerationContext context, int x, int z) {
        NoiseColumn column = context.chunkGenerator().getBaseColumn(
                x,
                z,
                context.heightAccessor(),
                context.randomState()
        );
        BlockPos.MutableBlockPos supportPos = new BlockPos.MutableBlockPos(x, MAX_STRUCTURE_Y - 1, z);

        for (int structureY = MAX_STRUCTURE_Y; structureY >= MIN_STRUCTURE_Y; structureY--) {
            BlockState support = column.getBlock(structureY - 1);
            if (!support.getFluidState().isEmpty()
                    || !support.isFaceSturdy(
                    EmptyBlockGetter.INSTANCE,
                    supportPos.setY(structureY - 1),
                    Direction.UP
            )) {
                continue;
            }

            boolean hasClearance = true;
            for (int offsetY = 0; offsetY < REQUIRED_CENTER_CLEARANCE; offsetY++) {
                if (!column.getBlock(structureY + offsetY).isAir()) {
                    hasClearance = false;
                    break;
                }
            }
            if (hasClearance) {
                return OptionalInt.of(structureY);
            }
        }
        return OptionalInt.empty();
    }

    @Override
    public StructureType<?> type() {
        return ModStructures.ANCIENT_FORGE.get();
    }
}

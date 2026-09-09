package com.yukari.relicera.common.worldgen.structure;

import com.yukari.relicera.ReliceraMod;
import com.yukari.relicera.registry.ModEntityTypes;
import com.yukari.relicera.registry.ModStructures;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.vehicle.MinecartChest;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.armortrim.ArmorTrim;
import net.minecraft.world.item.armortrim.TrimMaterial;
import net.minecraft.world.item.armortrim.TrimMaterials;
import net.minecraft.world.item.armortrim.TrimPattern;
import net.minecraft.world.item.armortrim.TrimPatterns;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.TemplateStructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

import java.util.Set;

public final class AncientForgePiece extends TemplateStructurePiece {
    private static final ResourceLocation TEMPLATE =
            ResourceLocation.fromNamespaceAndPath(ReliceraMod.MOD_ID, "ancient_forge");
    private static final ResourceLocation MINECART_LOOT_TABLE =
            ResourceLocation.fromNamespaceAndPath(ReliceraMod.MOD_ID, "chests/ancient_forge/minecart");
    private static final BlockPos ROTATION_PIVOT = new BlockPos(16, 0, 16);
    private static final int TEMPLATE_SIZE_X = 33;
    private static final int TEMPLATE_SIZE_Z = 33;
    private static final int MAX_FOUNDATION_DEPTH = 12;
    private static final Set<Block> FOUNDATION_TOP_BLOCKS = Set.of(
            Blocks.POLISHED_BLACKSTONE,
            Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS,
            Blocks.BLACKSTONE,
            Blocks.POLISHED_BLACKSTONE_STAIRS,
            Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS,
            Blocks.GILDED_BLACKSTONE,
            Blocks.POLISHED_BLACKSTONE_BRICKS,
            Blocks.CHISELED_POLISHED_BLACKSTONE,
            Blocks.POLISHED_BASALT,
            Blocks.MAGMA_BLOCK,
            Blocks.COAL_BLOCK,
            Blocks.BLACK_WOOL,
            Blocks.BLACKSTONE_STAIRS
    );

    public AncientForgePiece(StructureTemplateManager templateManager, BlockPos position, Rotation rotation) {
        super(
                ModStructures.ANCIENT_FORGE_PIECE.get(),
                0,
                templateManager,
                TEMPLATE,
                TEMPLATE.toString(),
                makeSettings(rotation),
                position
        );
    }

    public AncientForgePiece(StructureTemplateManager templateManager, CompoundTag tag) {
        super(
                ModStructures.ANCIENT_FORGE_PIECE.get(),
                tag,
                templateManager,
                ignored -> makeSettings(Rotation.valueOf(tag.getString("Rot")))
        );
    }

    private static StructurePlaceSettings makeSettings(Rotation rotation) {
        return new StructurePlaceSettings()
                .setRotation(rotation)
                .setMirror(Mirror.NONE)
                .setRotationPivot(ROTATION_PIVOT)
                .addProcessor(BlockIgnoreProcessor.STRUCTURE_BLOCK);
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        super.addAdditionalSaveData(context, tag);
        tag.putString("Rot", this.placeSettings.getRotation().name());
    }

    @Override
    protected void handleDataMarker(
            String metadata,
            BlockPos pos,
            ServerLevelAccessor level,
            RandomSource random,
            BoundingBox boundingBox
    ) {
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
        switch (metadata) {
            case "armorstand_trimmed" -> spawnTrimmedArmorStand(level, pos);
            case "minecart_loot" -> spawnLootMinecart(level, pos, random);
            case "spawn_piglin" -> spawnPersistentMob(EntityType.PIGLIN, level, pos, random);
            case "spawn_piglin_brute" -> spawnPersistentMob(EntityType.PIGLIN_BRUTE, level, pos, random);
            case "spawn_relicera_forgeling" -> spawnPersistentMob(ModEntityTypes.FORGELING.get(), level, pos, random);
            default -> {
            }
        }
    }

    private void spawnTrimmedArmorStand(ServerLevelAccessor level, BlockPos pos) {
        ArmorStand armorStand = new ArmorStand(
                level.getLevel(),
                pos.getX() + 0.5D,
                pos.getY(),
                pos.getZ() + 0.5D
        );
        float yaw = this.placeSettings.getRotation().rotate(Direction.SOUTH).toYRot();
        armorStand.moveTo(armorStand.getX(), armorStand.getY(), armorStand.getZ(), yaw, 0.0F);

        RegistryAccess registryAccess = level.registryAccess();
        Holder<TrimMaterial> gold = registryAccess.registryOrThrow(Registries.TRIM_MATERIAL)
                .getHolderOrThrow(TrimMaterials.GOLD);
        Holder<TrimPattern> snout = registryAccess.registryOrThrow(Registries.TRIM_PATTERN)
                .getHolderOrThrow(TrimPatterns.SNOUT);
        ArmorTrim trim = new ArmorTrim(gold, snout);

        ItemStack chestplate = new ItemStack(Items.GOLDEN_CHESTPLATE);
        ItemStack boots = new ItemStack(Items.GOLDEN_BOOTS);
        ArmorTrim.setTrim(registryAccess, chestplate, trim);
        ArmorTrim.setTrim(registryAccess, boots, trim);
        armorStand.setItemSlot(EquipmentSlot.CHEST, chestplate);
        armorStand.setItemSlot(EquipmentSlot.FEET, boots);
        level.addFreshEntity(armorStand);
    }

    private static void spawnLootMinecart(ServerLevelAccessor level, BlockPos markerPos, RandomSource random) {
        BlockPos railPos = markerPos.below();
        MinecartChest minecart = new MinecartChest(
                level.getLevel(),
                railPos.getX() + 0.5D,
                railPos.getY() + 0.0625D,
                railPos.getZ() + 0.5D
        );
        minecart.setLootTable(MINECART_LOOT_TABLE, random.nextLong());
        level.addFreshEntity(minecart);
    }

    private static <T extends Mob> void spawnPersistentMob(
            EntityType<T> entityType,
            ServerLevelAccessor level,
            BlockPos pos,
            RandomSource random
    ) {
        T mob = entityType.create(level.getLevel());
        if (mob == null) {
            return;
        }

        float yaw = random.nextFloat() * 360.0F;
        mob.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, yaw, 0.0F);
        mob.finalizeSpawn(
                level,
                level.getCurrentDifficultyAt(pos),
                MobSpawnType.STRUCTURE,
                null,
                null
        );
        mob.setPersistenceRequired();
        level.addFreshEntityWithPassengers(mob);
    }

    @Override
    public void postProcess(
            WorldGenLevel level,
            StructureManager structureManager,
            ChunkGenerator chunkGenerator,
            RandomSource random,
            BoundingBox chunkBoundingBox,
            ChunkPos chunkPos,
            BlockPos pivot
    ) {
        super.postProcess(level, structureManager, chunkGenerator, random, chunkBoundingBox, chunkPos, pivot);
        placeBlackstoneFoundation(level, chunkBoundingBox);
    }

    private void placeBlackstoneFoundation(WorldGenLevel level, BoundingBox chunkBoundingBox) {
        for (int localX = 0; localX < TEMPLATE_SIZE_X; localX++) {
            for (int localZ = 0; localZ < TEMPLATE_SIZE_Z; localZ++) {
                BlockPos transformed = StructureTemplate.calculateRelativePosition(
                        this.placeSettings,
                        new BlockPos(localX, 0, localZ)
                );
                BlockPos floorPos = this.templatePosition.offset(transformed);
                if (!chunkBoundingBox.isInside(floorPos)
                        || !FOUNDATION_TOP_BLOCKS.contains(level.getBlockState(floorPos).getBlock())) {
                    continue;
                }

                BlockPos.MutableBlockPos foundationPos = floorPos.mutable().move(Direction.DOWN);
                for (int depth = 0; depth < MAX_FOUNDATION_DEPTH; depth++) {
                    BlockState current = level.getBlockState(foundationPos);
                    if (current.getFluidState().isEmpty()
                            && current.isFaceSturdy(EmptyBlockGetter.INSTANCE, foundationPos, Direction.UP)) {
                        break;
                    }
                    level.setBlock(foundationPos, Blocks.BLACKSTONE.defaultBlockState(), 2);
                    foundationPos.move(Direction.DOWN);
                }
            }
        }
    }
}

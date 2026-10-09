package com.yukari.relicera.common.entity.forgeling;

import com.yukari.relicera.common.recipe.ForgelingSmithingRecipe;
import com.yukari.relicera.registry.ModRecipeTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

final class ForgelingSmithingTask {
    private final ForgelingSmithingRecipe recipe;
    private final UUID firstItem;
    private final UUID secondItem;
    private int completedStrikes;

    private ForgelingSmithingTask(Candidate candidate) {
        this.recipe = candidate.recipe;
        this.firstItem = candidate.first.getUUID();
        this.secondItem = candidate.second.getUUID();
    }

    static Optional<ForgelingSmithingTask> tryCreate(ServerLevel level, BlockPos tablePos) {
        return findUniqueCandidate(level, tablePos).map(ForgelingSmithingTask::new);
    }

    boolean isValid(ServerLevel level, BlockPos tablePos) {
        return findInputs(level, tablePos).isPresent();
    }

    StrikeResult strike(ServerLevel level, BlockPos tablePos) {
        Optional<Inputs> inputs = findInputs(level, tablePos);
        if (inputs.isEmpty()) {
            return StrikeResult.INVALID;
        }

        level.playSound(null, tablePos, SoundEvents.ANVIL_PLACE, SoundSource.BLOCKS, 0.8F, 0.7F);
        this.completedStrikes++;
        if (this.completedStrikes < this.recipe.requiredJumps()) {
            return StrikeResult.CONTINUE;
        }
        finish(level, tablePos, inputs.get());
        return StrikeResult.COMPLETED;
    }

    private Optional<Inputs> findInputs(ServerLevel level, BlockPos tablePos) {
        Entity firstEntity = level.getEntity(this.firstItem);
        Entity secondEntity = level.getEntity(this.secondItem);
        if (!(firstEntity instanceof ItemEntity first)
                || !(secondEntity instanceof ItemEntity second)
                || !first.isAlive()
                || !second.isAlive()
                || !inputArea(tablePos).contains(first.position())
                || !inputArea(tablePos).contains(second.position())
                || !this.recipe.matches(first.getItem(), second.getItem())) {
            return Optional.empty();
        }
        return Optional.of(new Inputs(first, second));
    }

    private void finish(ServerLevel level, BlockPos tablePos, Inputs inputs) {
        ItemEntity output = new ItemEntity(
                level,
                tablePos.getX() + 0.5D,
                tablePos.getY() + 1.25D,
                tablePos.getZ() + 0.5D,
                this.recipe.result()
        );
        consumeOne(inputs.first);
        consumeOne(inputs.second);
        output.setDefaultPickUpDelay();
        level.addFreshEntity(output);
    }

    private static Optional<Candidate> findUniqueCandidate(ServerLevel level, BlockPos tablePos) {
        List<ItemEntity> items = level.getEntitiesOfClass(
                ItemEntity.class,
                inputArea(tablePos),
                item -> item.isAlive() && !item.getItem().isEmpty()
        );
        if (items.size() < 2) {
            return Optional.empty();
        }

        List<ForgelingSmithingRecipe> recipes = level.getRecipeManager()
                .getAllRecipesFor(ModRecipeTypes.FORGELING_SMITHING.get());
        Map<ResourceLocation, Candidate> matches = new LinkedHashMap<>();
        Vec3 center = Vec3.atCenterOf(tablePos.above());
        for (ForgelingSmithingRecipe recipe : recipes) {
            for (int firstIndex = 0; firstIndex < items.size() - 1; firstIndex++) {
                for (int secondIndex = firstIndex + 1; secondIndex < items.size(); secondIndex++) {
                    ItemEntity first = items.get(firstIndex);
                    ItemEntity second = items.get(secondIndex);
                    if (!recipe.matches(first.getItem(), second.getItem())) {
                        continue;
                    }

                    double distance = first.distanceToSqr(center) + second.distanceToSqr(center);
                    Candidate candidate = new Candidate(recipe, first, second, distance);
                    Candidate previous = matches.get(recipe.getId());
                    if (previous == null || candidate.distance < previous.distance) {
                        matches.put(recipe.getId(), candidate);
                    }
                }
            }
        }
        return matches.size() == 1 ? Optional.of(matches.values().iterator().next()) : Optional.empty();
    }

    private static AABB inputArea(BlockPos tablePos) {
        double centerX = tablePos.getX() + 0.5D;
        double centerZ = tablePos.getZ() + 0.5D;
        return new AABB(
                centerX - 0.9D, tablePos.getY() + 0.85D, centerZ - 0.9D,
                centerX + 0.9D, tablePos.getY() + 2.25D, centerZ + 0.9D
        );
    }

    private static void consumeOne(ItemEntity entity) {
        ItemStack remainder = entity.getItem().copy();
        remainder.shrink(1);
        if (remainder.isEmpty()) {
            entity.discard();
        } else {
            entity.setItem(remainder);
        }
    }

    enum StrikeResult {
        INVALID,
        CONTINUE,
        COMPLETED
    }

    private record Candidate(
            ForgelingSmithingRecipe recipe,
            ItemEntity first,
            ItemEntity second,
            double distance
    ) {
    }

    private record Inputs(ItemEntity first, ItemEntity second) {
    }
}


package com.yukari.relicera.common.recipe.relicrepair;

import com.yukari.relicera.registry.ModRecipeTypes;
import java.util.Optional;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.ItemStackHandler;

public final class RelicRepairRecipes {
    private RelicRepairRecipes() {
    }

    public static Optional<RelicRepairRecipe> findMatching(Level level, ItemStackHandler itemHandler) {
        SimpleContainer inputs = new SimpleContainer(RelicRepairRecipe.INPUT_SLOT_COUNT);
        for (int slot = 0; slot < RelicRepairRecipe.INPUT_SLOT_COUNT; slot++) {
            inputs.setItem(slot, itemHandler.getStackInSlot(slot));
        }
        return level.getRecipeManager().getRecipeFor(ModRecipeTypes.RELIC_REPAIR.get(), inputs, level);
    }

    public static Optional<RelicRepairRecipe> findForRelic(Level level, ItemStack relic) {
        if (relic.isEmpty()) {
            return Optional.empty();
        }

        return level.getRecipeManager()
                .getAllRecipesFor(ModRecipeTypes.RELIC_REPAIR.get())
                .stream()
                .filter(recipe -> recipe.matchesRelic(relic))
                .findFirst();
    }

    public static boolean acceptsCatalyst(Level level, ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }

        return level.getRecipeManager()
                .getAllRecipesFor(ModRecipeTypes.RELIC_REPAIR.get())
                .stream()
                .anyMatch(recipe -> recipe.matchesCatalyst(stack));
    }

    public static boolean acceptsRelic(Level level, ItemStack stack) {
        return findForRelic(level, stack).isPresent();
    }
}

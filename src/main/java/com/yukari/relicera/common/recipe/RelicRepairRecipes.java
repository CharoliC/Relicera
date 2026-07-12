package com.yukari.relicera.common.recipe;

import com.yukari.relicera.registry.ModItems;
import java.util.List;
import java.util.Optional;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

public final class RelicRepairRecipes {
    private static final int DEFAULT_REPAIR_TIME = 200;

    private static final List<RelicRepairRecipe> RECIPES = List.of(
            new RelicRepairRecipe(
                    ModItems.EXTINGUISHED_SOLAR_FURNACE.get(),
                    1,
                    List.of(
                            Ingredient.of(ModItems.WARFIRE_FRAGMENT.get()),
                            Ingredient.of(ModItems.SOLAR_EMBER.get()),
                            Ingredient.of(Items.NETHERITE_INGOT)
                    ),
                    new ItemStack(ModItems.GRANBELLS_FURNACE.get()),
                    DEFAULT_REPAIR_TIME,
                    List.of(
                            "container.relicera.relic_repair_table.hint.granbells_furnace.warfire_fragment",
                            "container.relicera.relic_repair_table.hint.granbells_furnace.solar_ember",
                            "container.relicera.relic_repair_table.hint.granbells_furnace.netherite_ingot"
                    ),
                    RelicRepairParticleStyle.SOLAR_FURNACE
            ),
            new RelicRepairRecipe(
                    ModItems.WITHERED_LIFE_CHALICE.get(),
                    1,
                    List.of(
                            Ingredient.of(ModItems.REVIVAL_NECTAR.get()),
                            Ingredient.of(ModItems.CHALICE_LINING.get()),
                            Ingredient.of(Items.TOTEM_OF_UNDYING)
                    ),
                    new ItemStack(ModItems.ILUTHIAS_CHALICE.get()),
                    DEFAULT_REPAIR_TIME,
                    List.of(
                            "container.relicera.relic_repair_table.hint.iluthias_chalice.revival_nectar",
                            "container.relicera.relic_repair_table.hint.iluthias_chalice.chalice_lining",
                            "container.relicera.relic_repair_table.hint.iluthias_chalice.totem_of_undying"
                    ),
                    RelicRepairParticleStyle.LIFE_CHALICE
            ),
            new RelicRepairRecipe(
                    ModItems.DRIED_CROWN.get(),
                    1,
                    List.of(
                            Ingredient.of(ModItems.STORMSCALE.get()),
                            Ingredient.of(ModItems.RIPPLEHEART_PEARL.get()),
                            Ingredient.of(Items.CONDUIT)
                    ),
                    new ItemStack(ModItems.NEREIAS_CROWN.get()),
                    DEFAULT_REPAIR_TIME,
                    List.of(
                            "container.relicera.relic_repair_table.hint.nereias_crown.stormscale",
                            "container.relicera.relic_repair_table.hint.nereias_crown.rippleheart_pearl",
                            "container.relicera.relic_repair_table.hint.nereias_crown.conduit"
                    ),
                    RelicRepairParticleStyle.NEREIAS_CROWN
            )
    );

    private RelicRepairRecipes() {
    }

    public static Optional<RelicRepairRecipe> findForRelic(ItemStack relic) {
        if (relic.isEmpty()) {
            return Optional.empty();
        }

        return RECIPES.stream()
                .filter(recipe -> recipe.matchesRelic(relic))
                .findFirst();
    }
}

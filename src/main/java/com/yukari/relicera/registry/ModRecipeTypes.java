package com.yukari.relicera.registry;

import com.yukari.relicera.ReliceraMod;
import com.yukari.relicera.common.recipe.relicrepair.RelicRepairRecipe;
import com.yukari.relicera.common.recipe.ForgelingSmithingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModRecipeTypes {
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
            DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, ReliceraMod.MOD_ID);

    public static final RegistryObject<RecipeType<RelicRepairRecipe>> RELIC_REPAIR =
            RECIPE_TYPES.register("relic_repair", () -> new RecipeType<>() {
                @Override
                public String toString() {
                    return ReliceraMod.MOD_ID + ":relic_repair";
                }
            });

    public static final RegistryObject<RecipeType<ForgelingSmithingRecipe>> FORGELING_SMITHING =
            RECIPE_TYPES.register("forgeling_smithing", () -> new RecipeType<>() {
                @Override
                public String toString() {
                    return ReliceraMod.MOD_ID + ":forgeling_smithing";
                }
            });

    private ModRecipeTypes() {
    }

    public static void register(IEventBus eventBus) {
        RECIPE_TYPES.register(eventBus);
    }
}

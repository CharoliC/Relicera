package com.yukari.relicera.registry;

import com.yukari.relicera.ReliceraMod;
import com.yukari.relicera.common.recipe.NoRemainderShapelessRecipe;
import com.yukari.relicera.common.recipe.ForgelingSmithingRecipe;
import com.yukari.relicera.common.recipe.relicrepair.RelicRepairRecipe;
import com.yukari.relicera.common.recipe.RippleheartRingsRebindingRecipe;
import com.yukari.relicera.common.recipe.WaterBottleShapelessRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModRecipeSerializers {
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, ReliceraMod.MOD_ID);

    public static final RegistryObject<RecipeSerializer<NoRemainderShapelessRecipe>> NO_REMAINDER_SHAPELESS =
            RECIPE_SERIALIZERS.register("no_remainder_shapeless", NoRemainderShapelessRecipe.Serializer::new);

    public static final RegistryObject<RecipeSerializer<WaterBottleShapelessRecipe>> WATER_BOTTLE_SHAPELESS =
            RECIPE_SERIALIZERS.register("water_bottle_shapeless", WaterBottleShapelessRecipe.Serializer::new);

    public static final RegistryObject<RecipeSerializer<RelicRepairRecipe>> RELIC_REPAIR =
            RECIPE_SERIALIZERS.register("relic_repair", RelicRepairRecipe.Serializer::new);

    public static final RegistryObject<RecipeSerializer<ForgelingSmithingRecipe>> FORGELING_SMITHING =
            RECIPE_SERIALIZERS.register("forgeling_smithing", ForgelingSmithingRecipe.Serializer::new);

    public static final RegistryObject<RecipeSerializer<RippleheartRingsRebindingRecipe>> RIPPLEHEART_RINGS_REBINDING =
            RECIPE_SERIALIZERS.register("rippleheart_rings_rebinding", RippleheartRingsRebindingRecipe.Serializer::new);

    private ModRecipeSerializers() {
    }

    public static void register(IEventBus eventBus) {
        RECIPE_SERIALIZERS.register(eventBus);
    }
}

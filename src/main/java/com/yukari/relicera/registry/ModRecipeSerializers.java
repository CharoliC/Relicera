package com.yukari.relicera.registry;

import com.yukari.relicera.ReliceraMod;
import com.yukari.relicera.common.recipe.NoRemainderShapelessRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModRecipeSerializers {
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, ReliceraMod.MOD_ID);

    public static final RegistryObject<RecipeSerializer<NoRemainderShapelessRecipe>> NO_REMAINDER_SHAPELESS =
            RECIPE_SERIALIZERS.register("no_remainder_shapeless", NoRemainderShapelessRecipe.Serializer::new);

    private ModRecipeSerializers() {
    }

    public static void register(IEventBus eventBus) {
        RECIPE_SERIALIZERS.register(eventBus);
    }
}

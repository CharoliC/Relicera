package com.yukari.relicera.common.recipe;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.yukari.relicera.common.item.AstralStorybookItem;
import com.yukari.relicera.registry.ModItems;
import com.yukari.relicera.registry.ModRecipeSerializers;
import javax.annotation.Nullable;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;

public final class AstralStorybookRecipe extends ShapedRecipe {
    private final int storyId;

    private AstralStorybookRecipe(ShapedRecipe recipe, int storyId) {
        super(
                recipe.getId(),
                recipe.getGroup(),
                recipe.category(),
                recipe.getWidth(),
                recipe.getHeight(),
                recipe.getIngredients(),
                createStorybookResult(recipe.getResultItem(RegistryAccess.EMPTY), storyId),
                recipe.showNotification()
        );
        this.storyId = storyId;
    }

    @Override
    public boolean matches(CraftingContainer container, Level level) {
        if (!super.matches(container, level)) {
            return false;
        }

        for (int slot = 0; slot < container.getContainerSize(); slot++) {
            ItemStack stack = container.getItem(slot);
            if (stack.is(ModItems.ASTRAL_STORYBOOK.get()) && AstralStorybookItem.getStoryId(stack) != 0) {
                return false;
            }
        }
        return true;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.ASTRAL_STORYBOOK.get();
    }

    private static ItemStack createStorybookResult(ItemStack original, int storyId) {
        if (!original.is(ModItems.ASTRAL_STORYBOOK.get())) {
            throw new JsonParseException("Astral Storybook recipes must output relicera:astral_storybook");
        }
        ItemStack result = original.copy();
        AstralStorybookItem.setStoryId(result, storyId);
        return result;
    }

    public static final class Serializer implements RecipeSerializer<AstralStorybookRecipe> {
        @Override
        public AstralStorybookRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
            ShapedRecipe recipe = RecipeSerializer.SHAPED_RECIPE.fromJson(recipeId, json);
            return new AstralStorybookRecipe(recipe, readStoryId(json));
        }

        @Override
        @Nullable
        public AstralStorybookRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            ShapedRecipe recipe = RecipeSerializer.SHAPED_RECIPE.fromNetwork(recipeId, buffer);
            if (recipe == null) {
                return null;
            }
            int storyId = buffer.readVarInt();
            if (!AstralStorybookItem.isKnownStory(storyId)) {
                throw new IllegalArgumentException("Unknown Astral Storybook story id " + storyId);
            }
            return new AstralStorybookRecipe(recipe, storyId);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, AstralStorybookRecipe recipe) {
            RecipeSerializer.SHAPED_RECIPE.toNetwork(buffer, recipe);
            buffer.writeVarInt(recipe.storyId);
        }

        private static int readStoryId(JsonObject json) {
            int storyId = GsonHelper.getAsInt(json, "story");
            if (!AstralStorybookItem.isKnownStory(storyId)) {
                throw new JsonParseException("Unknown Astral Storybook story id " + storyId);
            }
            return storyId;
        }
    }
}

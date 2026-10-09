package com.yukari.relicera.common.recipe;

import com.google.gson.JsonObject;
import com.yukari.relicera.registry.ModRecipeSerializers;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.SmithingTransformRecipe;
import net.minecraftforge.common.ForgeHooks;

public final class TemplatelessSmithingRecipe extends SmithingTransformRecipe {
    private final Ingredient base;
    private final Ingredient addition;
    private final ItemStack result;

    public TemplatelessSmithingRecipe(ResourceLocation id, Ingredient base, Ingredient addition, ItemStack result) {
        super(id, Ingredient.EMPTY, base, addition, result);
        this.base = base;
        this.addition = addition;
        this.result = result;
    }

    @Override
    public boolean isIncomplete() {
        return ForgeHooks.hasNoElements(base) || ForgeHooks.hasNoElements(addition);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.TEMPLATELESS_SMITHING.get();
    }

    public Ingredient base() {
        return base;
    }

    public Ingredient addition() {
        return addition;
    }

    public static final class Serializer implements RecipeSerializer<TemplatelessSmithingRecipe> {
        @Override
        public TemplatelessSmithingRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
            Ingredient base = Ingredient.fromJson(GsonHelper.getNonNull(json, "base"));
            Ingredient addition = Ingredient.fromJson(GsonHelper.getNonNull(json, "addition"));
            ItemStack result = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result"));
            return new TemplatelessSmithingRecipe(recipeId, base, addition, result);
        }

        @Override
        public TemplatelessSmithingRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            return new TemplatelessSmithingRecipe(recipeId,
                    Ingredient.fromNetwork(buffer), Ingredient.fromNetwork(buffer), buffer.readItem());
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, TemplatelessSmithingRecipe recipe) {
            recipe.base.toNetwork(buffer);
            recipe.addition.toNetwork(buffer);
            buffer.writeItem(recipe.result);
        }
    }
}

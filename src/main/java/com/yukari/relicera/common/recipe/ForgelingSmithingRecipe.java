package com.yukari.relicera.common.recipe;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.yukari.relicera.registry.ModRecipeSerializers;
import com.yukari.relicera.registry.ModRecipeTypes;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

public final class ForgelingSmithingRecipe implements Recipe<SimpleContainer> {
    private static final int DEFAULT_REQUIRED_JUMPS = 6;

    private final ResourceLocation id;
    private final Ingredient first;
    private final Ingredient second;
    private final ItemStack result;
    private final int requiredJumps;

    public ForgelingSmithingRecipe(
            ResourceLocation id,
            Ingredient first,
            Ingredient second,
            ItemStack result,
            int requiredJumps
    ) {
        if (result.isEmpty()) {
            throw new IllegalArgumentException("Forgeling smithing recipes must have a non-empty result.");
        }
        if (requiredJumps < 1) {
            throw new IllegalArgumentException("Forgeling smithing recipes must require at least one jump.");
        }

        this.id = id;
        this.first = first;
        this.second = second;
        this.result = result.copy();
        this.requiredJumps = requiredJumps;
    }

    @Override
    public boolean matches(SimpleContainer container, Level level) {
        return container.getContainerSize() >= 2
                && matches(container.getItem(0), container.getItem(1));
    }

    public boolean matches(ItemStack firstStack, ItemStack secondStack) {
        return (first.test(firstStack) && second.test(secondStack))
                || (first.test(secondStack) && second.test(firstStack));
    }

    @Override
    public ItemStack assemble(SimpleContainer container, RegistryAccess registryAccess) {
        return result();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        return result();
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> ingredients = NonNullList.create();
        ingredients.add(first);
        ingredients.add(second);
        return ingredients;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.FORGELING_SMITHING.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipeTypes.FORGELING_SMITHING.get();
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    public ItemStack result() {
        return result.copy();
    }

    public Ingredient first() {
        return first;
    }

    public Ingredient second() {
        return second;
    }

    public int requiredJumps() {
        return requiredJumps;
    }

    public static final class Serializer implements RecipeSerializer<ForgelingSmithingRecipe> {
        @Override
        public ForgelingSmithingRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
            Ingredient first = Ingredient.fromJson(GsonHelper.getNonNull(json, "first"));
            Ingredient second = Ingredient.fromJson(GsonHelper.getNonNull(json, "second"));
            ItemStack result = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result"));
            int requiredJumps = GsonHelper.getAsInt(json, "required_jumps", DEFAULT_REQUIRED_JUMPS);
            if (requiredJumps < 1) {
                throw new JsonParseException("Forgeling smithing required_jumps must be at least one.");
            }
            return new ForgelingSmithingRecipe(recipeId, first, second, result, requiredJumps);
        }

        @Nullable
        @Override
        public ForgelingSmithingRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            return new ForgelingSmithingRecipe(
                    recipeId,
                    Ingredient.fromNetwork(buffer),
                    Ingredient.fromNetwork(buffer),
                    buffer.readItem(),
                    buffer.readVarInt()
            );
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, ForgelingSmithingRecipe recipe) {
            recipe.first.toNetwork(buffer);
            recipe.second.toNetwork(buffer);
            buffer.writeItem(recipe.result);
            buffer.writeVarInt(recipe.requiredJumps);
        }
    }
}

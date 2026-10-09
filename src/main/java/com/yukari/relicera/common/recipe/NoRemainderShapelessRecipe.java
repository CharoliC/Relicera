package com.yukari.relicera.common.recipe;

import com.google.gson.JsonObject;
import com.yukari.relicera.registry.ModRecipeSerializers;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

public class NoRemainderShapelessRecipe implements CraftingRecipe {
    private final ShapelessRecipe delegate;

    public NoRemainderShapelessRecipe(ShapelessRecipe delegate) {
        this.delegate = delegate;
    }

    @Override
    public boolean matches(CraftingContainer container, Level level) {
        return delegate.matches(container, level);
    }

    @Override
    public ItemStack assemble(CraftingContainer container, RegistryAccess registryAccess) {
        return delegate.assemble(container, registryAccess);
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return delegate.canCraftInDimensions(width, height);
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        return delegate.getResultItem(registryAccess);
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingContainer container) {
        return NonNullList.withSize(container.getContainerSize(), ItemStack.EMPTY);
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return delegate.getIngredients();
    }

    @Override
    public String getGroup() {
        return delegate.getGroup();
    }

    @Override
    public CraftingBookCategory category() {
        return delegate.category();
    }

    @Override
    public boolean isSpecial() {
        return delegate.isSpecial();
    }

    @Override
    public boolean showNotification() {
        return delegate.showNotification();
    }

    @Override
    public ResourceLocation getId() {
        return delegate.getId();
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.NO_REMAINDER_SHAPELESS.get();
    }

    public static class Serializer implements RecipeSerializer<NoRemainderShapelessRecipe> {
        private final ShapelessRecipe.Serializer vanillaSerializer = new ShapelessRecipe.Serializer();

        @Override
        public NoRemainderShapelessRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
            return new NoRemainderShapelessRecipe(vanillaSerializer.fromJson(recipeId, json));
        }

        @Override
        @Nullable
        public NoRemainderShapelessRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            ShapelessRecipe recipe = vanillaSerializer.fromNetwork(recipeId, buffer);
            return recipe == null ? null : new NoRemainderShapelessRecipe(recipe);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, NoRemainderShapelessRecipe recipe) {
            vanillaSerializer.toNetwork(buffer, recipe.delegate);
        }
    }
}

package com.yukari.relicera.common.recipe;

import com.google.gson.JsonObject;
import com.yukari.relicera.registry.ModRecipeSerializers;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

public class WaterBottleShapelessRecipe implements CraftingRecipe {
    private final ShapelessRecipe delegate;

    public WaterBottleShapelessRecipe(ShapelessRecipe delegate) {
        this.delegate = delegate;
    }

    @Override
    public boolean matches(CraftingContainer container, Level level) {
        if (!delegate.matches(container, level)) {
            return false;
        }

        for (int slot = 0; slot < container.getContainerSize(); slot++) {
            ItemStack stack = container.getItem(slot);
            if (stack.is(Items.POTION) && PotionUtils.getPotion(stack) != Potions.WATER) {
                return false;
            }
        }

        return true;
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
        return delegate.getRemainingItems(container);
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
        return ModRecipeSerializers.WATER_BOTTLE_SHAPELESS.get();
    }

    public static class Serializer implements RecipeSerializer<WaterBottleShapelessRecipe> {
        private final ShapelessRecipe.Serializer vanillaSerializer = new ShapelessRecipe.Serializer();

        @Override
        public WaterBottleShapelessRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
            return new WaterBottleShapelessRecipe(vanillaSerializer.fromJson(recipeId, json));
        }

        @Override
        @Nullable
        public WaterBottleShapelessRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            ShapelessRecipe recipe = vanillaSerializer.fromNetwork(recipeId, buffer);
            return recipe == null ? null : new WaterBottleShapelessRecipe(recipe);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, WaterBottleShapelessRecipe recipe) {
            vanillaSerializer.toNetwork(buffer, recipe.delegate);
        }
    }
}

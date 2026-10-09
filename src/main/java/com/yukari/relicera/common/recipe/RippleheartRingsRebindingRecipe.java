package com.yukari.relicera.common.recipe;

import com.google.gson.JsonObject;
import com.yukari.relicera.common.item.RippleheartRingItem;
import com.yukari.relicera.registry.ModItems;
import com.yukari.relicera.registry.ModRecipeSerializers;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
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
import net.minecraft.world.level.Level;

public class RippleheartRingsRebindingRecipe implements CraftingRecipe {
    private final ResourceLocation id;

    public RippleheartRingsRebindingRecipe(ResourceLocation id) {
        this.id = id;
    }

    @Override
    public boolean matches(CraftingContainer container, Level level) {
        return findSharedPairId(container).isPresent();
    }

    @Override
    public ItemStack assemble(CraftingContainer container, RegistryAccess registryAccess) {
        Optional<UUID> pairId = findSharedPairId(container);
        if (pairId.isEmpty()) {
            return ItemStack.EMPTY;
        }

        ItemStack result = new ItemStack(ModItems.RIPPLEHEART_RINGS.get());
        RippleheartRingItem.setPairId(result, pairId.get());
        return result;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        return new ItemStack(ModItems.RIPPLEHEART_RINGS.get());
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> ingredients = NonNullList.create();
        ingredients.add(Ingredient.of(ModItems.RIPPLEHEART_RING_RED.get()));
        ingredients.add(Ingredient.of(ModItems.RIPPLEHEART_RING_BLUE.get()));
        return ingredients;
    }

    @Override
    public CraftingBookCategory category() {
        return CraftingBookCategory.MISC;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public boolean showNotification() {
        return false;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.RIPPLEHEART_RINGS_REBINDING.get();
    }

    private static Optional<UUID> findSharedPairId(CraftingContainer container) {
        ItemStack redRing = ItemStack.EMPTY;
        ItemStack blueRing = ItemStack.EMPTY;

        for (int slot = 0; slot < container.getContainerSize(); slot++) {
            ItemStack stack = container.getItem(slot);
            if (stack.isEmpty()) {
                continue;
            }
            if (stack.is(ModItems.RIPPLEHEART_RING_RED.get()) && redRing.isEmpty()) {
                redRing = stack;
            } else if (stack.is(ModItems.RIPPLEHEART_RING_BLUE.get()) && blueRing.isEmpty()) {
                blueRing = stack;
            } else {
                return Optional.empty();
            }
        }

        Optional<UUID> redPairId = RippleheartRingItem.getPairId(redRing);
        Optional<UUID> bluePairId = RippleheartRingItem.getPairId(blueRing);
        return redPairId.isPresent() && redPairId.equals(bluePairId) ? redPairId : Optional.empty();
    }

    public static class Serializer implements RecipeSerializer<RippleheartRingsRebindingRecipe> {
        @Override
        public RippleheartRingsRebindingRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
            return new RippleheartRingsRebindingRecipe(recipeId);
        }

        @Override
        @Nullable
        public RippleheartRingsRebindingRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            return new RippleheartRingsRebindingRecipe(recipeId);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, RippleheartRingsRebindingRecipe recipe) {
        }
    }
}

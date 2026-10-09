package com.yukari.relicera.common.recipe.relicrepair;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.yukari.relicera.registry.ModRecipeSerializers;
import com.yukari.relicera.registry.ModRecipeTypes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
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

public final class RelicRepairRecipe implements Recipe<SimpleContainer> {
    public static final int MATERIAL_COUNT = 3;
    public static final int INPUT_SLOT_COUNT = 5;
    public static final int SLOT_CATALYST = 0;
    public static final int SLOT_RELIC = 1;
    public static final int SLOT_MATERIAL_1 = 2;

    private final ResourceLocation id;
    private final Ingredient brokenRelic;
    private final CountedIngredient catalyst;
    private final List<CountedIngredient> materials;
    private final ItemStack output;
    private final int repairTime;
    private final List<Component> hints;
    private final RelicRepairParticleStyle particleStyle;

    public RelicRepairRecipe(
            ResourceLocation id,
            Ingredient brokenRelic,
            CountedIngredient catalyst,
            List<CountedIngredient> materials,
            ItemStack output,
            int repairTime,
            List<Component> hints,
            RelicRepairParticleStyle particleStyle
    ) {
        if (materials.size() != MATERIAL_COUNT) {
            throw new IllegalArgumentException("Relic repair recipes must have exactly " + MATERIAL_COUNT + " material ingredients.");
        }
        if (output.isEmpty()) {
            throw new IllegalArgumentException("Relic repair recipes must have a non-empty output.");
        }
        if (repairTime < 1) {
            throw new IllegalArgumentException("Relic repair time must be at least one tick.");
        }
        if (hints.size() != MATERIAL_COUNT) {
            throw new IllegalArgumentException("Relic repair recipes must have exactly " + MATERIAL_COUNT + " material hints.");
        }

        this.id = id;
        this.brokenRelic = brokenRelic;
        this.catalyst = catalyst;
        this.materials = List.copyOf(materials);
        this.output = output.copy();
        this.repairTime = repairTime;
        this.hints = List.copyOf(hints);
        this.particleStyle = particleStyle == null ? RelicRepairParticleStyle.NONE : particleStyle;
    }

    @Override
    public boolean matches(SimpleContainer container, Level level) {
        if (container.getContainerSize() < INPUT_SLOT_COUNT) {
            return false;
        }
        if (!catalyst.test(container.getItem(SLOT_CATALYST))
                || !matchesRelic(container.getItem(SLOT_RELIC))) {
            return false;
        }

        for (int index = 0; index < MATERIAL_COUNT; index++) {
            if (!matchesMaterial(index, container.getItem(SLOT_MATERIAL_1 + index))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack assemble(SimpleContainer container, RegistryAccess registryAccess) {
        return output();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= INPUT_SLOT_COUNT;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        return output();
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> ingredients = NonNullList.create();
        ingredients.add(catalyst.ingredient());
        ingredients.add(brokenRelic);
        materials.stream().map(CountedIngredient::ingredient).forEach(ingredients::add);
        return ingredients;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.RELIC_REPAIR.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipeTypes.RELIC_REPAIR.get();
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    public Ingredient brokenRelic() {
        return brokenRelic;
    }

    public CountedIngredient catalyst() {
        return catalyst;
    }

    public List<CountedIngredient> materials() {
        return materials;
    }

    public ItemStack output() {
        return output.copy();
    }

    public int repairTime() {
        return repairTime;
    }

    public RelicRepairParticleStyle particleStyle() {
        return particleStyle;
    }

    public boolean matchesRelic(ItemStack stack) {
        return brokenRelic.test(stack);
    }

    public boolean matchesCatalyst(ItemStack stack) {
        return catalyst.matchesItem(stack);
    }

    public boolean matchesMaterial(int index, ItemStack stack) {
        return materials.get(index).test(stack);
    }

    public int materialCost(int index) {
        return materials.get(index).count();
    }

    public Component hint(int index) {
        return hints.get(index).copy();
    }

    public record CountedIngredient(Ingredient ingredient, int count) {
        public CountedIngredient {
            if (count < 1) {
                throw new IllegalArgumentException("Relic repair ingredient counts must be at least one.");
            }
        }

        public boolean test(ItemStack stack) {
            return stack.getCount() >= count && matchesItem(stack);
        }

        public boolean matchesItem(ItemStack stack) {
            return ingredient.test(stack);
        }

        public List<ItemStack> displayStacks() {
            return Arrays.stream(ingredient.getItems())
                    .map(ItemStack::copy)
                    .peek(stack -> stack.setCount(count))
                    .toList();
        }
    }

    public static final class Serializer implements RecipeSerializer<RelicRepairRecipe> {
        private static final int DEFAULT_REPAIR_TIME = 200;

        @Override
        public RelicRepairRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
            Ingredient brokenRelic = Ingredient.fromJson(GsonHelper.getNonNull(json, "relic"));
            CountedIngredient catalyst = readCountedIngredient(
                    GsonHelper.getNonNull(json, "catalyst"),
                    "Relic repair catalyst"
            );
            List<CountedIngredient> materials = readMaterials(GsonHelper.getAsJsonArray(json, "materials"));
            ItemStack output = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result"));
            int repairTime = GsonHelper.getAsInt(json, "repair_time", DEFAULT_REPAIR_TIME);
            List<Component> hints = readHints(json);
            RelicRepairParticleStyle particleStyle = RelicRepairParticleStyle.fromSerializedName(
                    GsonHelper.getAsString(json, "particle_style", RelicRepairParticleStyle.NONE.getSerializedName())
            );

            return new RelicRepairRecipe(
                    recipeId,
                    brokenRelic,
                    catalyst,
                    materials,
                    output,
                    repairTime,
                    hints,
                    particleStyle
            );
        }

        @Nullable
        @Override
        public RelicRepairRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            Ingredient brokenRelic = Ingredient.fromNetwork(buffer);
            CountedIngredient catalyst = new CountedIngredient(Ingredient.fromNetwork(buffer), buffer.readVarInt());
            List<CountedIngredient> materials = new ArrayList<>(MATERIAL_COUNT);
            for (int index = 0; index < MATERIAL_COUNT; index++) {
                materials.add(new CountedIngredient(Ingredient.fromNetwork(buffer), buffer.readVarInt()));
            }
            ItemStack output = buffer.readItem();
            int repairTime = buffer.readVarInt();
            List<Component> hints = new ArrayList<>(MATERIAL_COUNT);
            for (int index = 0; index < MATERIAL_COUNT; index++) {
                hints.add(buffer.readComponent());
            }
            RelicRepairParticleStyle particleStyle = RelicRepairParticleStyle.fromSerializedName(buffer.readUtf());
            return new RelicRepairRecipe(recipeId, brokenRelic, catalyst, materials, output, repairTime, hints, particleStyle);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, RelicRepairRecipe recipe) {
            recipe.brokenRelic.toNetwork(buffer);
            recipe.catalyst.ingredient().toNetwork(buffer);
            buffer.writeVarInt(recipe.catalyst.count());
            for (CountedIngredient material : recipe.materials) {
                material.ingredient().toNetwork(buffer);
                buffer.writeVarInt(material.count());
            }
            buffer.writeItem(recipe.output);
            buffer.writeVarInt(recipe.repairTime);
            for (Component hint : recipe.hints) {
                buffer.writeComponent(hint);
            }
            buffer.writeUtf(recipe.particleStyle.getSerializedName());
        }

        private static List<CountedIngredient> readMaterials(JsonArray json) {
            if (json.size() != MATERIAL_COUNT) {
                throw new JsonParseException("Relic repair recipes must have exactly " + MATERIAL_COUNT + " materials.");
            }

            List<CountedIngredient> materials = new ArrayList<>(MATERIAL_COUNT);
            for (JsonElement element : json) {
                if (!element.isJsonObject()) {
                    throw new JsonParseException("Relic repair materials must be JSON objects.");
                }
                materials.add(readCountedIngredient(element, "Relic repair material"));
            }
            return materials;
        }

        private static CountedIngredient readCountedIngredient(JsonElement element, String description) {
            if (!element.isJsonObject()) {
                throw new JsonParseException(description + " must be a JSON object.");
            }
            JsonObject countedJson = element.getAsJsonObject();
            int count = GsonHelper.getAsInt(countedJson, "count", 1);
            JsonElement ingredientJson = countedJson.has("ingredient")
                    ? countedJson.get("ingredient")
                    : countedJson;
            return new CountedIngredient(Ingredient.fromJson(ingredientJson), count);
        }

        private static List<Component> readHints(JsonObject json) {
            if (!json.has("hints")) {
                return List.of(Component.empty(), Component.empty(), Component.empty());
            }

            JsonArray hintsJson = GsonHelper.getAsJsonArray(json, "hints");
            if (hintsJson.size() != MATERIAL_COUNT) {
                throw new JsonParseException("Relic repair recipes must have exactly " + MATERIAL_COUNT + " hints when hints are present.");
            }

            List<Component> hints = new ArrayList<>(MATERIAL_COUNT);
            for (JsonElement hintJson : hintsJson) {
                Component hint = Component.Serializer.fromJson(hintJson);
                if (hint == null) {
                    throw new JsonParseException("Relic repair hints must be valid text components.");
                }
                hints.add(hint);
            }
            return hints;
        }
    }
}

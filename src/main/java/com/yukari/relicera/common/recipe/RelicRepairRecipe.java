package com.yukari.relicera.common.recipe;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

public record RelicRepairRecipe(
        Item brokenRelic,
        int feysilverCost,
        List<Ingredient> materials,
        ItemStack output,
        int repairTime,
        List<String> hintTranslationKeys,
        RelicRepairParticleStyle particleStyle
) {
    public static final int MATERIAL_COUNT = 3;

    public RelicRepairRecipe {
        if (materials.size() != MATERIAL_COUNT) {
            throw new IllegalArgumentException("Relic repair recipes must have exactly " + MATERIAL_COUNT + " material ingredients.");
        }
        if (hintTranslationKeys.size() != MATERIAL_COUNT) {
            throw new IllegalArgumentException("Relic repair recipes must have exactly " + MATERIAL_COUNT + " hint translation keys.");
        }
        output = output.copy();
        if (particleStyle == null) {
            particleStyle = RelicRepairParticleStyle.NONE;
        }
    }

    public boolean matchesRelic(ItemStack stack) {
        return stack.is(brokenRelic);
    }

    public boolean matchesMaterial(int index, ItemStack stack) {
        return materials.get(index).test(stack);
    }

    public Component hint(int index) {
        return Component.translatable(hintTranslationKeys.get(index));
    }

    @Override
    public ItemStack output() {
        return output.copy();
    }
}

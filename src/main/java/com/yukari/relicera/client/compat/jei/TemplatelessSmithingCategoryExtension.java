package com.yukari.relicera.client.compat.jei;

import com.yukari.relicera.common.recipe.TemplatelessSmithingRecipe;
import mezz.jei.api.gui.builder.IIngredientAcceptor;
import mezz.jei.api.recipe.category.extensions.vanilla.smithing.ISmithingCategoryExtension;
import net.minecraft.core.RegistryAccess;

public final class TemplatelessSmithingCategoryExtension implements ISmithingCategoryExtension<TemplatelessSmithingRecipe> {
    @Override
    public <T extends IIngredientAcceptor<T>> void setTemplate(TemplatelessSmithingRecipe recipe, T ingredients) {
    }

    @Override
    public <T extends IIngredientAcceptor<T>> void setBase(TemplatelessSmithingRecipe recipe, T ingredients) {
        ingredients.addIngredients(recipe.base());
    }

    @Override
    public <T extends IIngredientAcceptor<T>> void setAddition(TemplatelessSmithingRecipe recipe, T ingredients) {
        ingredients.addIngredients(recipe.addition());
    }

    @Override
    public <T extends IIngredientAcceptor<T>> void setOutput(TemplatelessSmithingRecipe recipe, T ingredients) {
        ingredients.addItemStack(recipe.getResultItem(RegistryAccess.EMPTY));
    }
}

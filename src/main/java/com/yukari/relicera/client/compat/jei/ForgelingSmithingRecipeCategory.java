package com.yukari.relicera.client.compat.jei;

import com.yukari.relicera.ReliceraMod;
import com.yukari.relicera.common.recipe.ForgelingSmithingRecipe;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public final class ForgelingSmithingRecipeCategory implements IRecipeCategory<ForgelingSmithingRecipe> {
    public static final RecipeType<ForgelingSmithingRecipe> TYPE =
            RecipeType.create(ReliceraMod.MOD_ID, "forgeling_smithing", ForgelingSmithingRecipe.class);

    private static final ResourceLocation BACKGROUND_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            ReliceraMod.MOD_ID,
            "textures/gui/jei/forgeling_smithing.png"
    );
    private static final ResourceLocation ICON_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            ReliceraMod.MOD_ID,
            "textures/gui/jei/forgeling_smithing_icon.png"
    );
    private static final int WIDTH = 128;
    private static final int TEXTURE_HEIGHT = 64;
    private static final int BACKGROUND_V = 10;
    private static final int HEIGHT = 48;
    private static final int FIRST_INPUT_X = 9;
    private static final int FIRST_INPUT_Y = 14;
    private static final int SECOND_INPUT_X = 35;
    private static final int SECOND_INPUT_Y = 14;
    private static final int OUTPUT_X = 99;
    private static final int OUTPUT_Y = 14;

    private final IDrawableStatic background;
    private final IDrawable icon;

    public ForgelingSmithingRecipeCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.drawableBuilder(BACKGROUND_TEXTURE, 0, BACKGROUND_V, WIDTH, HEIGHT)
                .setTextureSize(WIDTH, TEXTURE_HEIGHT)
                .build();
        this.icon = guiHelper.drawableBuilder(ICON_TEXTURE, 0, 0, 16, 16)
                .setTextureSize(16, 16)
                .build();
    }

    @Override
    public RecipeType<ForgelingSmithingRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.relicera.category.forgeling_smithing");
    }

    @Override
    public int getWidth() {
        return WIDTH;
    }

    @Override
    public int getHeight() {
        return HEIGHT;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(
            IRecipeLayoutBuilder builder,
            ForgelingSmithingRecipe recipe,
            IFocusGroup focuses
    ) {
        builder.addInputSlot(FIRST_INPUT_X, FIRST_INPUT_Y)
                .addIngredients(recipe.first());
        builder.addInputSlot(SECOND_INPUT_X, SECOND_INPUT_Y)
                .addIngredients(recipe.second());
        builder.addOutputSlot(OUTPUT_X, OUTPUT_Y)
                .addItemStack(recipe.result());
    }

    @Override
    public ResourceLocation getRegistryName(ForgelingSmithingRecipe recipe) {
        return recipe.getId();
    }

    @Override
    public void draw(
            ForgelingSmithingRecipe recipe,
            IRecipeSlotsView recipeSlotsView,
            GuiGraphics guiGraphics,
            double mouseX,
            double mouseY
    ) {
        background.draw(guiGraphics);
    }
}

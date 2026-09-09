package com.yukari.relicera.client.compat.jei;

import com.yukari.relicera.ReliceraMod;
import com.yukari.relicera.common.recipe.relicrepair.RelicRepairRecipe;
import com.yukari.relicera.registry.ModBlocks;
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
import net.minecraft.Util;

public final class RelicRepairRecipeCategory implements IRecipeCategory<RelicRepairRecipe> {
    public static final RecipeType<RelicRepairRecipe> TYPE =
            RecipeType.create(ReliceraMod.MOD_ID, "relic_repair", RelicRepairRecipe.class);

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            ReliceraMod.MOD_ID,
            "textures/gui/relic_repair_table.png"
    );
    private static final int BACKGROUND_U = 41;
    private static final int BACKGROUND_V = 6;
    private static final int WIDTH = 94;
    private static final int HEIGHT = 69;
    private static final int CATALYST_X = 13;
    private static final int CATALYST_Y = 15;
    private static final int RELIC_X = 39;
    private static final int RELIC_Y = 6;
    private static final int MATERIAL_1_X = 65;
    private static final int MATERIAL_1_Y = 15;
    private static final int MATERIAL_2_X = 4;
    private static final int MATERIAL_2_Y = 39;
    private static final int MATERIAL_3_X = 74;
    private static final int MATERIAL_3_Y = 39;
    private static final int OUTPUT_X = 39;
    private static final int OUTPUT_Y = 48;
    private static final int ARROW_X = 41;
    private static final int ARROW_Y = 26;
    private static final int ARROW_U = 0;
    private static final int ARROW_V = 166;
    private static final int ARROW_WIDTH = 13;
    private static final int ARROW_HEIGHT = 14;

    private final IDrawableStatic background;
    private final IDrawable icon;

    public RelicRepairRecipeCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createDrawable(TEXTURE, BACKGROUND_U, BACKGROUND_V, WIDTH, HEIGHT);
        this.icon = guiHelper.createDrawableItemLike(ModBlocks.RELIC_REPAIR_TABLE.get());
    }

    @Override
    public RecipeType<RelicRepairRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("container.relicera.relic_repair_table.title");
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
    public void setRecipe(IRecipeLayoutBuilder builder, RelicRepairRecipe recipe, IFocusGroup focuses) {
        builder.addInputSlot(CATALYST_X, CATALYST_Y)
                .addItemStacks(recipe.catalyst().displayStacks());
        builder.addInputSlot(RELIC_X, RELIC_Y)
                .addIngredients(recipe.brokenRelic());
        builder.addInputSlot(MATERIAL_1_X, MATERIAL_1_Y)
                .addItemStacks(recipe.materials().get(0).displayStacks());
        builder.addInputSlot(MATERIAL_2_X, MATERIAL_2_Y)
                .addItemStacks(recipe.materials().get(1).displayStacks());
        builder.addInputSlot(MATERIAL_3_X, MATERIAL_3_Y)
                .addItemStacks(recipe.materials().get(2).displayStacks());
        builder.addOutputSlot(OUTPUT_X, OUTPUT_Y)
                .addItemStack(recipe.output());
    }

    @Override
    public ResourceLocation getRegistryName(RelicRepairRecipe recipe) {
        return recipe.getId();
    }

    @Override
    public void draw(
            RelicRepairRecipe recipe,
            IRecipeSlotsView recipeSlotsView,
            GuiGraphics guiGraphics,
            double mouseX,
            double mouseY
    ) {
        background.draw(guiGraphics);
        long elapsedTicks = Util.getMillis() / 50L;
        int arrowHeight = Math.max(
                1,
                (int) ((elapsedTicks % recipe.repairTime() + 1L) * ARROW_HEIGHT / recipe.repairTime())
        );
        guiGraphics.blit(TEXTURE, ARROW_X, ARROW_Y, ARROW_U, ARROW_V, ARROW_WIDTH, arrowHeight);
    }
}

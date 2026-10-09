package com.yukari.relicera.config;

import net.minecraftforge.common.ForgeConfigSpec;

public final class ModClientConfig {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec.IntValue NIGHT_GLOVES_SCULK_SHRIEKER_HIGHLIGHT_RANGE;
    public static final ForgeConfigSpec.IntValue RIPPLEHEART_RINGS_MAX_BOTTOM_ALPHA;
    public static final ForgeConfigSpec.BooleanValue JEI_SHOW_RELIC_REPAIR_RECIPES;
    public static final ForgeConfigSpec SPEC;

    static {
        BUILDER.push("night_gloves");

        NIGHT_GLOVES_SCULK_SHRIEKER_HIGHLIGHT_RANGE = BUILDER
                .comment("Range in blocks for highlighting sculk shriekers while Night Gloves are equipped.")
                .defineInRange("sculkShriekerHighlightRange", 24, 0, 96);

        BUILDER.pop();

        BUILDER.push("rippleheart_rings");

        RIPPLEHEART_RINGS_MAX_BOTTOM_ALPHA = BUILDER
                .comment("Maximum opacity of the active Rippleheart Ring glow at the bottom of the screen. 0 disables the glow.")
                .defineInRange("maxBottomAlpha", 50, 0, 255);

        BUILDER.pop();

        BUILDER.push("jei");

        JEI_SHOW_RELIC_REPAIR_RECIPES = BUILDER
                .comment("Whether JEI shows Relic Repair Table recipes.")
                .define("showRelicRepairRecipes", true);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }

    private ModClientConfig() {
    }
}

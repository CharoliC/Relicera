package com.yukari.relicera.config;

import net.minecraftforge.common.ForgeConfigSpec;

public final class ModClientConfig {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec.IntValue NIGHT_GLOVES_SCULK_SHRIEKER_HIGHLIGHT_RANGE;
    public static final ForgeConfigSpec SPEC;

    static {
        BUILDER.push("night_gloves");

        NIGHT_GLOVES_SCULK_SHRIEKER_HIGHLIGHT_RANGE = BUILDER
                .comment("Range in blocks for highlighting sculk shriekers while Night Gloves are equipped.")
                .defineInRange("sculkShriekerHighlightRange", 24, 0, 96);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }

    private ModClientConfig() {
    }
}

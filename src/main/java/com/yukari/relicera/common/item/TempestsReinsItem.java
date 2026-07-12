package com.yukari.relicera.common.item;

import com.yukari.relicera.ReliceraMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.HorseArmorItem;

public class TempestsReinsItem extends HorseArmorItem {
    private static final int ARMOR_PROTECTION = 16;
    private static final ResourceLocation ARMOR_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            ReliceraMod.MOD_ID,
            "textures/entity/horse/armor/horse_armor_tempests_reins.png"
    );

    public TempestsReinsItem(Properties properties) {
        super(ARMOR_PROTECTION, "tempests_reins", properties);
    }

    @Override
    public ResourceLocation getTexture() {
        return ARMOR_TEXTURE;
    }
}

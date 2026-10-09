package com.yukari.relicera.client.compat.jade;

import com.yukari.relicera.ReliceraMod;
import com.yukari.relicera.common.block.SculkFruitCropBlock;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.theme.IThemeHelper;

public enum SculkFruitGrowthProvider implements IBlockComponentProvider {
    INSTANCE;

    private static final ResourceLocation UID =
            ResourceLocation.fromNamespaceAndPath(ReliceraMod.MOD_ID, "sculk_fruit_growth");

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        int age = accessor.getBlockState().getValue(SculkFruitCropBlock.AGE);
        IThemeHelper theme = IThemeHelper.get();
        Component maturity = age >= SculkFruitCropBlock.MAX_AGE
                ? theme.success(Component.translatable("tooltip.jade.crop_mature"))
                : theme.info(age * 100 / SculkFruitCropBlock.MAX_AGE + "%");
        tooltip.add(Component.translatable("tooltip.jade.crop_growth", maturity));
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }
}

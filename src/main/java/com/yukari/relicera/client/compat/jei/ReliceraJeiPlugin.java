package com.yukari.relicera.client.compat.jei;

import com.yukari.relicera.ReliceraMod;
import com.yukari.relicera.registry.ModItems;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.stream.IntStream;

@JeiPlugin
public final class ReliceraJeiPlugin implements IModPlugin {
    private static final ResourceLocation PLUGIN_ID = ResourceLocation.fromNamespaceAndPath(ReliceraMod.MOD_ID, "item_info");

    @Override
    public ResourceLocation getPluginUid() {
        return PLUGIN_ID;
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        addInfo(registration, ModItems.FEYSILVER_INGOT.get(), "feysilver_ingot", 1);
        addInfo(registration, ModItems.FORGOTTEN_THREAD.get(), "forgotten_thread", 2);
        addInfo(registration, ModItems.ROTTEN_TUSK.get(), "rotten_tusk", 2);
        addInfo(registration, ModItems.EPHEMERAL_BLOOM.get(), "ephemeral_bloom", 2);
        addInfo(registration, ModItems.WARFIRE_FRAGMENT.get(), "warfire_fragment", 1);
        addInfo(registration, ModItems.STORMSCALE.get(), "stormscale", 1);
        addInfo(registration, ModItems.RIPPLEHEART_PEARL.get(), "rippleheart_pearl", 2);
        addInfo(registration, ModItems.ASTRAL_LENS.get(), "astral_lens", 2);
        addInfo(registration, ModItems.ASTRAL_STORYBOOK.get(), "astral_storybook", 1);
        addInfo(registration, ModItems.EXTINGUISHED_SOLAR_FURNACE.get(), "extinguished_solar_furnace", 3);
        addInfo(registration, ModItems.WITHERED_LIFE_CHALICE.get(), "withered_life_chalice", 3);
        addInfo(registration, ModItems.DRIED_CROWN.get(), "dried_crown", 3);
    }

    private static void addInfo(IRecipeRegistration registration, Item item, String itemName, int lineCount) {
        Component[] lines = IntStream.rangeClosed(1, lineCount)
                .mapToObj(line -> Component.translatable("jei.relicera.info." + itemName + ".line_" + line))
                .toArray(Component[]::new);
        registration.addItemStackInfo(new ItemStack(item), lines);
    }
}

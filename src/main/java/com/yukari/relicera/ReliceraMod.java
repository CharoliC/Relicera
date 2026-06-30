package com.yukari.relicera;

import com.mojang.logging.LogUtils;
import com.yukari.relicera.common.network.ModNetworking;
import com.yukari.relicera.config.ModClientConfig;
import com.yukari.relicera.config.ModServerConfig;
import com.yukari.relicera.registry.ModBlockEntities;
import com.yukari.relicera.registry.ModBlocks;
import com.yukari.relicera.registry.ModCreativeModeTabs;
import com.yukari.relicera.registry.ModEffects;
import com.yukari.relicera.registry.ModItems;
import com.yukari.relicera.registry.ModLootModifiers;
import com.yukari.relicera.registry.ModMenuTypes;
import com.yukari.relicera.registry.ModRecipeSerializers;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(ReliceraMod.MOD_ID)
public class ReliceraMod {
    public static final String MOD_ID = "relicera";
    private static final Logger LOGGER = LogUtils.getLogger();

    public ReliceraMod(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();

        context.registerConfig(ModConfig.Type.CLIENT, ModClientConfig.SPEC);
        context.registerConfig(ModConfig.Type.SERVER, ModServerConfig.SPEC);
        ModBlocks.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModEffects.register(modEventBus);
        ModItems.register(modEventBus);
        ModCreativeModeTabs.register(modEventBus);
        ModLootModifiers.register(modEventBus);
        ModMenuTypes.register(modEventBus);
        ModRecipeSerializers.register(modEventBus);
        ModNetworking.register();

        modEventBus.addListener(this::commonSetup);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("Relicera common setup complete.");
    }
}

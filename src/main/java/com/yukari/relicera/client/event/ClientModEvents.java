package com.yukari.relicera.client.event;

import com.yukari.relicera.ReliceraMod;
import com.yukari.relicera.client.screen.FourfoldSherdPendantScreen;
import com.yukari.relicera.client.renderer.RelicRepairTableRenderer;
import com.yukari.relicera.client.screen.RelicRepairTableScreen;
import com.yukari.relicera.client.tooltip.ClientIluthiasChaliceTooltip;
import com.yukari.relicera.common.tooltip.IluthiasChaliceTooltip;
import com.yukari.relicera.registry.ModBlockEntities;
import com.yukari.relicera.registry.ModMenuTypes;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = ReliceraMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientModEvents {
    private ClientModEvents() {
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(ModMenuTypes.RELIC_REPAIR_TABLE.get(), RelicRepairTableScreen::new);
            MenuScreens.register(ModMenuTypes.FOURFOLD_SHERD_PENDANT.get(), FourfoldSherdPendantScreen::new);
        });
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.RELIC_REPAIR_TABLE.get(), RelicRepairTableRenderer::new);
    }

    @SubscribeEvent
    public static void registerTooltipComponents(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(IluthiasChaliceTooltip.class, ClientIluthiasChaliceTooltip::new);
    }
}

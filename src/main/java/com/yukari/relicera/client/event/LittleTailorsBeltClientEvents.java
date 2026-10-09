package com.yukari.relicera.client.event;

import com.yukari.relicera.ReliceraMod;
import com.yukari.relicera.common.curio.LittleTailorsBeltClientData;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ReliceraMod.MOD_ID, value = Dist.CLIENT)
public final class LittleTailorsBeltClientEvents {
    private LittleTailorsBeltClientEvents() {
    }

    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        if (event.getLevel().isClientSide()) {
            LittleTailorsBeltClientData.clear();
        }
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        LittleTailorsBeltClientData.clear();
    }
}

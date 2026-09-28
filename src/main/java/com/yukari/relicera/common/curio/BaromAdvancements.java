package com.yukari.relicera.common.curio;

import com.yukari.relicera.ReliceraMod;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.advancements.Advancement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ReliceraMod.MOD_ID)
public final class BaromAdvancements {
    private static final ResourceLocation WIND_BURST_ADVANCEMENT =
            ResourceLocation.fromNamespaceAndPath(ReliceraMod.MOD_ID, "barom_2");
    private static final ResourceLocation COMPLETED_TABLET_ADVANCEMENT =
            ResourceLocation.fromNamespaceAndPath(ReliceraMod.MOD_ID, "barom_3");
    private static final Map<UUID, Integer> AIRBORNE_WIND_BURSTS = new HashMap<>();

    private BaromAdvancements() {
    }

    public static void recordWindBurst(ServerPlayer player) {
        int count = AIRBORNE_WIND_BURSTS.merge(player.getUUID(), 1, Integer::sum);
        if (count >= 5) {
            AIRBORNE_WIND_BURSTS.remove(player.getUUID());
            award(player, WIND_BURST_ADVANCEMENT, "five_wind_bursts_without_landing");
        }
    }

    public static void awardCompletedTablet(ServerPlayer player) {
        award(player, COMPLETED_TABLET_ADVANCEMENT, "has_completed_covenant_tablet");
    }

    private static void award(ServerPlayer player, ResourceLocation id, String criterion) {
        Advancement advancement = player.server.getAdvancements().getAdvancement(id);
        if (advancement != null) {
            player.getAdvancements().award(advancement, criterion);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.player instanceof ServerPlayer player
                && player.onGround()) {
            AIRBORNE_WIND_BURSTS.remove(player.getUUID());
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingDeath(LivingDeathEvent event) {
        if (!event.isCanceled() && event.getEntity() instanceof ServerPlayer player) {
            AIRBORNE_WIND_BURSTS.remove(player.getUUID());
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            AIRBORNE_WIND_BURSTS.remove(player.getUUID());
        }
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        AIRBORNE_WIND_BURSTS.clear();
    }
}

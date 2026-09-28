package com.yukari.relicera.client.effect;

import com.yukari.relicera.ReliceraMod;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Mod.EventBusSubscriber(modid = ReliceraMod.MOD_ID, value = Dist.CLIENT)
public final class CameraShakeEffect {
    private static final int MAX_ACTIVE_SHAKES = 8;
    private static final List<ActiveShake> ACTIVE_SHAKES = new ArrayList<>();

    private CameraShakeEffect() {
    }

    public static void start(int durationTicks, float intensity) {
        if (durationTicks <= 0 || intensity <= 0.0F) {
            return;
        }

        if (ACTIVE_SHAKES.size() >= MAX_ACTIVE_SHAKES) {
            ACTIVE_SHAKES.remove(0);
        }
        ACTIVE_SHAKES.add(new ActiveShake(durationTicks, intensity));
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null) {
            ACTIVE_SHAKES.clear();
            return;
        }
        if (minecraft.isPaused()) {
            return;
        }

        Iterator<ActiveShake> iterator = ACTIVE_SHAKES.iterator();
        while (iterator.hasNext()) {
            ActiveShake shake = iterator.next();
            if (--shake.remainingTicks <= 0) {
                iterator.remove();
            }
        }
    }

    @SubscribeEvent
    public static void onComputeCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        if (ACTIVE_SHAKES.isEmpty()) {
            return;
        }

        float partialTick = (float) event.getPartialTick();
        float yawOffset = 0.0F;
        float pitchOffset = 0.0F;
        float rollOffset = 0.0F;
        for (ActiveShake shake : ACTIVE_SHAKES) {
            float remaining = Mth.clamp(
                    (shake.remainingTicks - partialTick) / shake.durationTicks,
                    0.0F,
                    1.0F
            );
            float strength = shake.intensity * remaining * remaining;
            float elapsed = shake.durationTicks - shake.remainingTicks + partialTick;
            yawOffset += Mth.sin(elapsed * 3.7F) * 2.2F * strength;
            pitchOffset += Mth.sin(elapsed * 5.1F + 0.8F) * 1.6F * strength;
            rollOffset += Mth.sin(elapsed * 4.3F + 1.6F) * 0.8F * strength;
        }

        event.setYaw(event.getYaw() + Mth.clamp(yawOffset, -8.0F, 8.0F));
        event.setPitch(event.getPitch() + Mth.clamp(pitchOffset, -6.0F, 6.0F));
        event.setRoll(event.getRoll() + Mth.clamp(rollOffset, -4.0F, 4.0F));
    }

    private static final class ActiveShake {
        private final int durationTicks;
        private final float intensity;
        private int remainingTicks;

        private ActiveShake(int durationTicks, float intensity) {
            this.durationTicks = durationTicks;
            this.intensity = intensity;
            this.remainingTicks = durationTicks;
        }
    }
}

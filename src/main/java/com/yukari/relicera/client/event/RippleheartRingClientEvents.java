package com.yukari.relicera.client.event;

import com.mojang.blaze3d.systems.RenderSystem;
import com.yukari.relicera.ReliceraMod;
import com.yukari.relicera.common.curio.RippleheartRingClientData;
import com.yukari.relicera.common.curio.RippleheartRingEffects.ActiveState;
import com.yukari.relicera.config.ModClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ReliceraMod.MOD_ID, value = Dist.CLIENT)
public final class RippleheartRingClientEvents {
    private static final float FADE_STEP = 0.125F;
    private static final int RED_GLOW_RGB = 0xFF4F70;
    private static final int BLUE_GLOW_RGB = 0x4FB7FF;

    private static float previousIntensity;
    private static float intensity;
    private static ActiveState glowState = ActiveState.INACTIVE;

    private RippleheartRingClientEvents() {
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        previousIntensity = intensity;
        if (minecraft.level == null || minecraft.player == null) {
            RippleheartRingClientData.reset();
            intensity = 0.0F;
            previousIntensity = 0.0F;
            glowState = ActiveState.INACTIVE;
            return;
        }

        ActiveState activeState = RippleheartRingClientData.getActiveState();
        if (activeState != ActiveState.INACTIVE) {
            glowState = activeState;
        }
        float target = activeState == ActiveState.INACTIVE ? 0.0F : 1.0F;
        intensity += Mth.clamp(target - intensity, -FADE_STEP, FADE_STEP);
        if (intensity <= 0.0F && target == 0.0F) {
            glowState = ActiveState.INACTIVE;
        }
    }

    @SubscribeEvent
    public static void onRenderHud(RenderGuiOverlayEvent.Pre event) {
        if (!event.getOverlay().id().equals(VanillaGuiOverlay.HOTBAR.id())) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null
                || !minecraft.player.isAlive()
                || minecraft.screen != null
                || minecraft.options.hideGui
                || glowState == ActiveState.INACTIVE) {
            return;
        }

        float renderedIntensity = Mth.lerp(event.getPartialTick(), previousIntensity, intensity);
        if (renderedIntensity <= 0.0F) {
            return;
        }

        float pulse = 0.92F + 0.08F * Mth.sin((minecraft.player.tickCount + event.getPartialTick()) * 0.08F);
        int maxBottomAlpha = ModClientConfig.RIPPLEHEART_RINGS_MAX_BOTTOM_ALPHA.get();
        int bottomAlpha = Mth.clamp(Math.round(maxBottomAlpha * renderedIntensity * pulse), 0, 255);
        if (bottomAlpha <= 0) {
            return;
        }
        int rgb = glowState == ActiveState.RED ? RED_GLOW_RGB : BLUE_GLOW_RGB;
        int topColor = rgb;
        int bottomColor = bottomAlpha << 24 | rgb;

        int screenWidth = event.getWindow().getGuiScaledWidth();
        int screenHeight = event.getWindow().getGuiScaledHeight();
        int glowHeight = Math.min(screenHeight, Math.max(36, screenHeight / 5));
        GuiGraphics graphics = event.getGuiGraphics();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        graphics.fillGradient(0, screenHeight - glowHeight, screenWidth, screenHeight, topColor, bottomColor);
        RenderSystem.disableBlend();
    }
}

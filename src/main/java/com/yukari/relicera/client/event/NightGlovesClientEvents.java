package com.yukari.relicera.client.event;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.yukari.relicera.ReliceraMod;
import com.yukari.relicera.common.curio.NightGlovesEffects;
import com.yukari.relicera.config.ModClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = ReliceraMod.MOD_ID, value = Dist.CLIENT)
public final class NightGlovesClientEvents {
    private static final int SCAN_INTERVAL_TICKS = 20;
    private static final List<BlockPos> HIGHLIGHTED_SHRIEKERS = new ArrayList<>();
    private static int ticksUntilScan;

    private NightGlovesClientEvents() {
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null) {
            HIGHLIGHTED_SHRIEKERS.clear();
            ticksUntilScan = 0;
            return;
        }

        if (ticksUntilScan-- > 0) {
            return;
        }

        ticksUntilScan = SCAN_INTERVAL_TICKS;
        HIGHLIGHTED_SHRIEKERS.clear();
        if (!NightGlovesEffects.isEquipped(minecraft.player)) {
            return;
        }

        int range = ModClientConfig.NIGHT_GLOVES_SCULK_SHRIEKER_HIGHLIGHT_RANGE.get();
        if (range <= 0) {
            return;
        }

        BlockPos playerPos = minecraft.player.blockPosition();
        BlockPos.betweenClosed(
                playerPos.offset(-range, -range, -range),
                playerPos.offset(range, range, range)
        ).forEach(pos -> {
            if (minecraft.level.getBlockState(pos).is(Blocks.SCULK_SHRIEKER)
                    && playerPos.distSqr(pos) <= range * range) {
                HIGHLIGHTED_SHRIEKERS.add(pos.immutable());
            }
        });
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS
                || HIGHLIGHTED_SHRIEKERS.isEmpty()) {
            return;
        }

        Vec3 cameraPosition = event.getCamera().getPosition();
        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.lines());

        RenderSystem.disableDepthTest();
        for (BlockPos pos : HIGHLIGHTED_SHRIEKERS) {
            AABB box = new AABB(pos).inflate(0.02D).move(-cameraPosition.x(), -cameraPosition.y(), -cameraPosition.z());
            LevelRenderer.renderLineBox(poseStack, vertexConsumer, box, 0.43F, 0.1F, 0.82F, 1.0F);
        }
        bufferSource.endBatch(RenderType.lines());
        RenderSystem.enableDepthTest();
    }
}

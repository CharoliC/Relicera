package com.yukari.relicera.client.effect;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.yukari.relicera.ReliceraMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Mod.EventBusSubscriber(modid = ReliceraMod.MOD_ID, value = Dist.CLIENT)
public final class LiftedBlockRingEffect {
    private static final int MAX_ACTIVE_BLOCKS = 256;
    private static final int GROUND_SCAN_ABOVE = 1;
    private static final int GROUND_SCAN_DEPTH = 5;
    private static final int MAX_FLIGHT_TICKS = 30;
    private static final int LANDING_HOLD_TICKS = 2;
    private static final double GRAVITY = 0.04D;
    private static final double DRAG = 0.98D;
    private static final double GROUND_OFFSET = 0.025D;
    private static final List<LiftedBlock> ACTIVE_BLOCKS = new ArrayList<>();
    private static ClientLevel activeLevel;

    private LiftedBlockRingEffect() {
    }

    public static void spawnRadialPulse(
            double centerX,
            double centerY,
            double centerZ,
            RadialPulse pulse
    ) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null
                || pulse == null
                || !pulse.isValid()
                || minecraft.options.particles().get() == ParticleStatus.MINIMAL) {
            return;
        }

        if (activeLevel != level) {
            ACTIVE_BLOCKS.clear();
            activeLevel = level;
        }

        double radiusSquared = pulse.radius() * pulse.radius();
        int centerBlockX = Mth.floor(centerX);
        int centerBlockZ = Mth.floor(centerZ);
        int minimumX = Mth.floor(centerX - pulse.radius());
        int maximumX = Mth.floor(centerX + pulse.radius());
        int minimumZ = Mth.floor(centerZ - pulse.radius());
        int maximumZ = Mth.floor(centerZ + pulse.radius());
        boolean decreasedParticles = minecraft.options.particles().get() == ParticleStatus.DECREASED;

        for (int blockX = minimumX; blockX <= maximumX; blockX++) {
            for (int blockZ = minimumZ; blockZ <= maximumZ; blockZ++) {
                double offsetX = blockX + 0.5D - centerX;
                double offsetZ = blockZ + 0.5D - centerZ;
                double distanceSquared = offsetX * offsetX + offsetZ * offsetZ;
                if (distanceSquared > radiusSquared) {
                    continue;
                }
                if (decreasedParticles
                        && ((blockX - centerBlockX + blockZ - centerBlockZ) & 1) != 0) {
                    continue;
                }

                BlockPos groundPos = findGround(level, blockX + 0.5D, centerY, blockZ + 0.5D);
                if (groundPos == null) {
                    continue;
                }

                double normalizedDistance = Mth.clamp(
                        Math.sqrt(distanceSquared) / pulse.radius(),
                        0.0D,
                        1.0D
                );
                double smoothedDistance = normalizedDistance
                        * normalizedDistance
                        * (3.0D - 2.0D * normalizedDistance);
                double upwardVelocity = Mth.lerp(
                        smoothedDistance,
                        pulse.centerUpwardVelocity(),
                        pulse.edgeUpwardVelocity()
                );
                int startDelayTicks = Mth.floor(
                        smoothedDistance * pulse.edgeDelayTicks() + 0.5D
                );

                ACTIVE_BLOCKS.add(new LiftedBlock(
                        groundPos,
                        level.getBlockState(groundPos),
                        groundPos.getX(),
                        groundPos.getY() + GROUND_OFFSET,
                        groundPos.getZ(),
                        upwardVelocity,
                        startDelayTicks,
                        pulse.scale()
                ));
            }
        }

        while (ACTIVE_BLOCKS.size() > MAX_ACTIVE_BLOCKS) {
            ACTIVE_BLOCKS.remove(0);
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null || activeLevel != minecraft.level) {
            ACTIVE_BLOCKS.clear();
            activeLevel = minecraft.level;
            return;
        }
        if (minecraft.isPaused()) {
            return;
        }

        Iterator<LiftedBlock> iterator = ACTIVE_BLOCKS.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().tick()) {
                iterator.remove();
            }
        }
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES || ACTIVE_BLOCKS.isEmpty()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null || activeLevel != level) {
            return;
        }

        Vec3 cameraPosition = event.getCamera().getPosition();
        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource bufferSource = minecraft.renderBuffers().bufferSource();
        BlockRenderDispatcher blockRenderer = minecraft.getBlockRenderer();
        RandomSource random = RandomSource.create();
        float partialTick = event.getPartialTick();

        for (LiftedBlock block : ACTIVE_BLOCKS) {
            if (!block.started) {
                continue;
            }

            double renderY = Mth.lerp(partialTick, block.previousY, block.y);
            poseStack.pushPose();
            poseStack.translate(
                    block.x - cameraPosition.x,
                    renderY - cameraPosition.y,
                    block.z - cameraPosition.z
            );
            poseStack.translate(0.5D, 0.5D, 0.5D);
            poseStack.scale(block.scale, block.scale, block.scale);
            poseStack.translate(-0.5D, -0.5D, -0.5D);

            VertexConsumer vertexConsumer = bufferSource.getBuffer(
                    ItemBlockRenderTypes.getMovingBlockRenderType(block.state)
            );
            blockRenderer.getModelRenderer().tesselateBlock(
                    level,
                    blockRenderer.getBlockModel(block.state),
                    block.state,
                    block.sourcePosition,
                    poseStack,
                    vertexConsumer,
                    false,
                    random,
                    block.state.getSeed(block.sourcePosition),
                    OverlayTexture.NO_OVERLAY
            );
            poseStack.popPose();
        }

        bufferSource.endBatch();
    }

    private static BlockPos findGround(ClientLevel level, double x, double y, double z) {
        int blockX = Mth.floor(x);
        int blockZ = Mth.floor(z);
        int startY = Math.min(Mth.floor(y) + GROUND_SCAN_ABOVE, level.getMaxBuildHeight() - 1);
        int endY = Math.max(level.getMinBuildHeight(), startY - GROUND_SCAN_DEPTH);
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos(blockX, startY, blockZ);

        for (int blockY = startY; blockY >= endY; blockY--) {
            cursor.setY(blockY);
            BlockState state = level.getBlockState(cursor);
            if (state.isAir()) {
                continue;
            }
            if (!state.getFluidState().isEmpty()) {
                return null;
            }
            if (state.getCollisionShape(level, cursor).isEmpty()) {
                continue;
            }
            if (state.hasBlockEntity() || state.getRenderShape() != RenderShape.MODEL) {
                return null;
            }
            return cursor.immutable();
        }
        return null;
    }

    public record RadialPulse(
            double radius,
            double centerUpwardVelocity,
            double edgeUpwardVelocity,
            int edgeDelayTicks,
            float scale
    ) {
        private boolean isValid() {
            return radius > 0.0D
                    && centerUpwardVelocity >= edgeUpwardVelocity
                    && edgeUpwardVelocity > GRAVITY
                    && edgeDelayTicks >= 0
                    && scale > 0.0F;
        }
    }

    private static final class LiftedBlock {
        private final BlockPos sourcePosition;
        private final BlockState state;
        private final double x;
        private final double groundY;
        private final double z;
        private final int startDelayTicks;
        private final float scale;
        private double y;
        private double previousY;
        private double velocityY;
        private int age;
        private int landedTicks;
        private boolean started;
        private boolean landed;

        private LiftedBlock(
                BlockPos sourcePosition,
                BlockState state,
                double x,
                double y,
                double z,
                double velocityY,
                int startDelayTicks,
                float scale
        ) {
            this.sourcePosition = sourcePosition;
            this.state = state;
            this.x = x;
            this.groundY = y;
            this.z = z;
            this.startDelayTicks = startDelayTicks;
            this.scale = scale;
            this.y = y;
            this.previousY = y;
            this.velocityY = velocityY;
        }

        private boolean tick() {
            previousY = y;
            if (age++ < startDelayTicks) {
                return false;
            }

            started = true;
            if (landed) {
                return ++landedTicks >= LANDING_HOLD_TICKS;
            }

            velocityY -= GRAVITY;
            y += velocityY;
            velocityY *= DRAG;

            if (y <= groundY && velocityY <= 0.0D) {
                y = groundY;
                velocityY = 0.0D;
                landed = true;
            }

            return age > startDelayTicks + MAX_FLIGHT_TICKS;
        }
    }
}

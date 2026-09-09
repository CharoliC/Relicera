package com.yukari.relicera.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.yukari.relicera.common.entity.forgeling.Forgeling;
import net.minecraft.client.model.EntityModel;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public final class ForgelingModel extends EntityModel<Forgeling> {
    private static final int VERTICAL_SEGMENTS = 8;
    private static final float MAX_SWAY_OFFSET = 0.075F;
    private static final float MIN_X = -4.0F / 16.0F;
    private static final float MAX_X = 4.0F / 16.0F;
    private static final float MIN_Y = 16.0F / 16.0F;
    private static final float MAX_Y = 24.0F / 16.0F;
    private static final float MIN_Z = -4.0F / 16.0F;
    private static final float MAX_Z = 4.0F / 16.0F;
    private float idleSway;

    @Override
    public void setupAnim(Forgeling forgeling, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        float partialTick = Math.max(0.0F, Math.min(1.0F, ageInTicks - forgeling.tickCount));
        this.idleSway = forgeling.getIdleSway(partialTick);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer consumer, int packedLight, int packedOverlay,
                               float red, float green, float blue, float alpha) {
        PoseStack.Pose pose = poseStack.last();

        // Split the side faces into one-pixel-high rings. Minecraft model-space Y grows downward,
        // so the inverse height weight keeps the actual bottom planted and bends the upper body.
        for (int segment = 0; segment < VERTICAL_SEGMENTS; segment++) {
            float bottom = (float) segment / VERTICAL_SEGMENTS;
            float top = (float) (segment + 1) / VERTICAL_SEGMENTS;
            float bottomY = MIN_Y + (MAX_Y - MIN_Y) * bottom;
            float topY = MIN_Y + (MAX_Y - MIN_Y) * top;
            float bottomOffset = swayOffset(bottom);
            float topOffset = swayOffset(top);

            // Keep every face wound outward so the mesh remains valid for culling render types.
            quad(pose, consumer, packedLight, packedOverlay, red, green, blue, alpha,
                    MAX_X + topOffset, topY, MIN_Z,
                    MAX_X + bottomOffset, bottomY, MIN_Z,
                    MIN_X + bottomOffset, bottomY, MIN_Z,
                    MIN_X + topOffset, topY, MIN_Z,
                    0.25F, lerp(0.0F, 0.25F, bottom), 0.0F, lerp(0.0F, 0.25F, top),
                    0.0F, 0.0F, -1.0F);
            quad(pose, consumer, packedLight, packedOverlay, red, green, blue, alpha,
                    MAX_X + topOffset, topY, MAX_Z,
                    MAX_X + bottomOffset, bottomY, MAX_Z,
                    MAX_X + bottomOffset, bottomY, MIN_Z,
                    MAX_X + topOffset, topY, MIN_Z,
                    0.25F, lerp(0.25F, 0.5F, bottom), 0.0F, lerp(0.25F, 0.5F, top),
                    1.0F, 0.0F, 0.0F);
            quad(pose, consumer, packedLight, packedOverlay, red, green, blue, alpha,
                    MIN_X + topOffset, topY, MAX_Z,
                    MIN_X + bottomOffset, bottomY, MAX_Z,
                    MAX_X + bottomOffset, bottomY, MAX_Z,
                    MAX_X + topOffset, topY, MAX_Z,
                    0.5F, lerp(0.0F, 0.25F, bottom), 0.25F, lerp(0.0F, 0.25F, top),
                    0.0F, 0.0F, 1.0F);
            quad(pose, consumer, packedLight, packedOverlay, red, green, blue, alpha,
                    MIN_X + topOffset, topY, MIN_Z,
                    MIN_X + bottomOffset, bottomY, MIN_Z,
                    MIN_X + bottomOffset, bottomY, MAX_Z,
                    MIN_X + topOffset, topY, MAX_Z,
                    0.5F, lerp(0.25F, 0.5F, bottom), 0.25F, lerp(0.25F, 0.5F, top),
                    -1.0F, 0.0F, 0.0F);
        }

        float actualTopOffset = swayOffset(0.0F);
        quad(pose, consumer, packedLight, packedOverlay, red, green, blue, alpha,
                MIN_X + actualTopOffset, MIN_Y, MAX_Z,
                MIN_X + actualTopOffset, MIN_Y, MIN_Z,
                MAX_X + actualTopOffset, MIN_Y, MIN_Z,
                MAX_X + actualTopOffset, MIN_Y, MAX_Z,
                0.25F, 0.75F, 0.0F, 0.5F, 0.0F, -1.0F, 0.0F);
        quad(pose, consumer, packedLight, packedOverlay, red, green, blue, alpha,
                MIN_X, MAX_Y, MIN_Z,
                MIN_X, MAX_Y, MAX_Z,
                MAX_X, MAX_Y, MAX_Z,
                MAX_X, MAX_Y, MIN_Z,
                0.75F, 0.0F, 0.5F, 0.25F, 0.0F, 1.0F, 0.0F);
    }

    private float swayOffset(float modelHeight) {
        return this.idleSway * MAX_SWAY_OFFSET * (float) Math.pow(1.0F - modelHeight, 1.6D);
    }

    private static float lerp(float start, float end, float amount) {
        return start + (end - start) * amount;
    }

    private static void quad(PoseStack.Pose pose, VertexConsumer consumer, int packedLight, int packedOverlay,
                             float red, float green, float blue, float alpha,
                             float x0, float y0, float z0, float x1, float y1, float z1,
                             float x2, float y2, float z2, float x3, float y3, float z3,
                             float minU, float minV, float maxU, float maxV,
                             float normalX, float normalY, float normalZ) {
        vertex(pose, consumer, packedLight, packedOverlay, red, green, blue, alpha,
                x0, y0, z0, minU, maxV, normalX, normalY, normalZ);
        vertex(pose, consumer, packedLight, packedOverlay, red, green, blue, alpha,
                x1, y1, z1, minU, minV, normalX, normalY, normalZ);
        vertex(pose, consumer, packedLight, packedOverlay, red, green, blue, alpha,
                x2, y2, z2, maxU, minV, normalX, normalY, normalZ);
        vertex(pose, consumer, packedLight, packedOverlay, red, green, blue, alpha,
                x3, y3, z3, maxU, maxV, normalX, normalY, normalZ);
    }

    private static void vertex(PoseStack.Pose pose, VertexConsumer consumer, int packedLight, int packedOverlay,
                               float red, float green, float blue, float alpha,
                               float x, float y, float z, float u, float v,
                               float normalX, float normalY, float normalZ) {
        Matrix4f poseMatrix = pose.pose();
        Matrix3f normalMatrix = pose.normal();
        consumer.vertex(poseMatrix, x, y, z)
                .color(red, green, blue, alpha)
                .uv(u, v)
                .overlayCoords(packedOverlay)
                .uv2(packedLight)
                .normal(normalMatrix, normalX, normalY, normalZ)
                .endVertex();
    }
}

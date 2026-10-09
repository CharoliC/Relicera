package com.yukari.relicera.client.model;

import com.yukari.relicera.ReliceraMod;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;

public final class TreasureHuntersGlovesModel extends HumanoidModel<LivingEntity> {
    private static final float GLOVE_DEFORMATION = 0.3F;
    private static final float RING_DEFORMATION = 0.4F;
    private static final float OUTLINE_DEFORMATION = 0.45F;

    public static final ModelLayerLocation WIDE_LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(ReliceraMod.MOD_ID, "treasure_hunters_gloves"),
            "main");
    public static final ModelLayerLocation SLIM_LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(ReliceraMod.MOD_ID, "treasure_hunters_gloves_slim"),
            "main");

    private final ModelPart rightGlove;
    private final ModelPart rightOutline;
    private final ModelPart leftGlove;
    private final ModelPart leftOutline;

    public TreasureHuntersGlovesModel(ModelPart root) {
        super(root);
        rightGlove = rightArm.getChild("right_glove");
        rightOutline = rightArm.getChild("right_outline");
        leftGlove = leftArm.getChild("left_glove");
        leftOutline = leftArm.getChild("left_outline");
    }

    public ModelPart arm(HumanoidArm arm) {
        return arm == HumanoidArm.LEFT ? leftArm : rightArm;
    }

    public ModelPart glove(HumanoidArm arm) {
        return arm == HumanoidArm.LEFT ? leftGlove : rightGlove;
    }

    public ModelPart outline(HumanoidArm arm) {
        return arm == HumanoidArm.LEFT ? leftOutline : rightOutline;
    }

    public static LayerDefinition createWideBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        addEmptyHumanoidParts(root);

        PartDefinition rightArm = root.addOrReplaceChild(
                "right_arm", CubeListBuilder.create(), PartPose.offset(-5.0F, 2.0F, 0.0F));
        rightArm.addOrReplaceChild(
                "right_glove",
                CubeListBuilder.create()
                        .texOffs(0, 8)
                        .addBox(-3.0F, 6.0F, -2.0F, 4.0F, 4.0F, 4.0F,
                                new CubeDeformation(GLOVE_DEFORMATION))
                        .texOffs(16, 8)
                        .addBox(-3.0F, 5.0F, -2.0F, 4.0F, 1.0F, 4.0F,
                                new CubeDeformation(RING_DEFORMATION)),
                PartPose.ZERO);
        rightArm.addOrReplaceChild(
                "right_outline",
                CubeListBuilder.create()
                        .texOffs(32, 8)
                        .addBox(1.0F, 10.0F, 2.0F, -4.0F, -4.0F, -4.0F,
                                new CubeDeformation(-OUTLINE_DEFORMATION)),
                PartPose.ZERO);

        PartDefinition leftArm = root.addOrReplaceChild(
                "left_arm", CubeListBuilder.create(), PartPose.offset(5.0F, 2.0F, 0.0F));
        leftArm.addOrReplaceChild(
                "left_glove",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-1.0F, 6.0F, -2.0F, 4.0F, 4.0F, 4.0F,
                                new CubeDeformation(GLOVE_DEFORMATION))
                        .texOffs(16, 13)
                        .addBox(-1.0F, 5.0F, -2.0F, 4.0F, 1.0F, 4.0F,
                                new CubeDeformation(RING_DEFORMATION)),
                PartPose.ZERO);
        leftArm.addOrReplaceChild(
                "left_outline",
                CubeListBuilder.create()
                        .texOffs(16, 24)
                        .addBox(3.0F, 10.0F, 2.0F, -4.0F, -4.0F, -4.0F,
                                new CubeDeformation(-OUTLINE_DEFORMATION)),
                PartPose.ZERO);

        return LayerDefinition.create(mesh, 32, 32);
    }

    public static LayerDefinition createSlimBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        addEmptyHumanoidParts(root);

        PartDefinition rightArm = root.addOrReplaceChild(
                "right_arm", CubeListBuilder.create(), PartPose.offset(-5.0F, 2.5F, 0.0F));
        rightArm.addOrReplaceChild(
                "right_glove",
                CubeListBuilder.create()
                        .texOffs(0, 8)
                        .addBox(-2.0F, 6.0F, -2.0F, 3.0F, 4.0F, 4.0F,
                                new CubeDeformation(GLOVE_DEFORMATION))
                        .texOffs(0, 16)
                        .addBox(-2.0F, 5.0F, -2.0F, 3.0F, 1.0F, 4.0F,
                                new CubeDeformation(RING_DEFORMATION)),
                PartPose.ZERO);
        rightArm.addOrReplaceChild(
                "right_outline",
                CubeListBuilder.create()
                        .texOffs(28, 16)
                        .addBox(1.0F, 10.0F, 2.0F, -3.0F, -4.0F, -4.0F,
                                new CubeDeformation(-OUTLINE_DEFORMATION)),
                PartPose.ZERO);

        PartDefinition leftArm = root.addOrReplaceChild(
                "left_arm", CubeListBuilder.create(), PartPose.offset(5.0F, 2.5F, 0.0F));
        leftArm.addOrReplaceChild(
                "left_glove",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-1.0F, 6.0F, -2.0F, 3.0F, 4.0F, 4.0F,
                                new CubeDeformation(GLOVE_DEFORMATION))
                        .texOffs(14, 16)
                        .addBox(-1.0F, 5.0F, -2.0F, 3.0F, 1.0F, 4.0F,
                                new CubeDeformation(RING_DEFORMATION)),
                PartPose.ZERO);
        leftArm.addOrReplaceChild(
                "left_outline",
                CubeListBuilder.create()
                        .texOffs(28, 8)
                        .addBox(2.0F, 10.0F, 2.0F, -3.0F, -4.0F, -4.0F,
                                new CubeDeformation(-OUTLINE_DEFORMATION)),
                PartPose.ZERO);

        return LayerDefinition.create(mesh, 32, 32);
    }

    private static void addEmptyHumanoidParts(PartDefinition root) {
        root.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.ZERO);
    }
}

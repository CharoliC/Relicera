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

public final class SurfaceGlovesModel extends HumanoidModel<LivingEntity> {
    private static final float GLOVE_DEFORMATION = 0.3F;

    public static final ModelLayerLocation LEATHER_WIDE_LAYER_LOCATION = layer("leather_gloves");
    public static final ModelLayerLocation LEATHER_SLIM_LAYER_LOCATION = layer("leather_gloves_slim");
    public static final ModelLayerLocation NIGHT_WIDE_LAYER_LOCATION = layer("night_gloves");
    public static final ModelLayerLocation NIGHT_SLIM_LAYER_LOCATION = layer("night_gloves_slim");
    public static final ModelLayerLocation THOUSANDWEIGHT_WIDE_LAYER_LOCATION = layer("thousandweight_gauntlets");
    public static final ModelLayerLocation THOUSANDWEIGHT_SLIM_LAYER_LOCATION = layer("thousandweight_gauntlets_slim");

    private final ModelPart rightGlove;
    private final ModelPart leftGlove;

    public SurfaceGlovesModel(ModelPart root) {
        super(root);
        rightGlove = rightArm.getChild("right_glove");
        leftGlove = leftArm.getChild("left_glove");
    }

    public ModelPart arm(HumanoidArm arm) {
        return arm == HumanoidArm.LEFT ? leftArm : rightArm;
    }

    public ModelPart glove(HumanoidArm arm) {
        return arm == HumanoidArm.LEFT ? leftGlove : rightGlove;
    }

    public static LayerDefinition createLeatherWideBodyLayer() {
        return createBodyLayer(false, 1, 0.35F, 0, 16, 16, 0);
    }

    public static LayerDefinition createLeatherSlimBodyLayer() {
        return createBodyLayer(true, 1, 0.35F, 14, 0, 14, 5);
    }

    public static LayerDefinition createNightWideBodyLayer() {
        return createBodyLayer(false, 1, 0.35F, 0, 16, 16, 0);
    }

    public static LayerDefinition createNightSlimBodyLayer() {
        return createBodyLayer(true, 1, 0.35F, 14, 5, 14, 0);
    }

    public static LayerDefinition createThousandweightWideBodyLayer() {
        return createBodyLayer(false, 2, 0.4F, 0, 16, 16, 0);
    }

    public static LayerDefinition createThousandweightSlimBodyLayer() {
        return createBodyLayer(true, 2, 0.4F, 14, 0, 14, 6);
    }

    private static LayerDefinition createBodyLayer(
            boolean slim,
            int cuffHeight,
            float cuffDeformation,
            int rightCuffU,
            int rightCuffV,
            int leftCuffU,
            int leftCuffV) {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        addEmptyHumanoidParts(root);

        float armY = slim ? 2.5F : 2.0F;
        float rightX = slim ? -2.0F : -3.0F;
        int gloveWidth = slim ? 3 : 4;
        float cuffY = 6.0F - cuffHeight;

        PartDefinition rightArm = root.addOrReplaceChild(
                "right_arm", CubeListBuilder.create(), PartPose.offset(-5.0F, armY, 0.0F));
        rightArm.addOrReplaceChild(
                "right_glove",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(rightX, 6.0F, -2.0F, gloveWidth, 4.0F, 4.0F,
                                new CubeDeformation(GLOVE_DEFORMATION))
                        .texOffs(rightCuffU, rightCuffV)
                        .addBox(rightX, cuffY, -2.0F, gloveWidth, cuffHeight, 4.0F,
                                new CubeDeformation(cuffDeformation)),
                PartPose.ZERO);

        PartDefinition leftArm = root.addOrReplaceChild(
                "left_arm", CubeListBuilder.create(), PartPose.offset(5.0F, armY, 0.0F));
        leftArm.addOrReplaceChild(
                "left_glove",
                CubeListBuilder.create()
                        .texOffs(0, 8)
                        .addBox(-1.0F, 6.0F, -2.0F, gloveWidth, 4.0F, 4.0F,
                                new CubeDeformation(GLOVE_DEFORMATION))
                        .texOffs(leftCuffU, leftCuffV)
                        .addBox(-1.0F, cuffY, -2.0F, gloveWidth, cuffHeight, 4.0F,
                                new CubeDeformation(cuffDeformation)),
                PartPose.ZERO);

        return LayerDefinition.create(mesh, 32, 32);
    }

    private static ModelLayerLocation layer(String path) {
        return new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(ReliceraMod.MOD_ID, path), "main");
    }

    private static void addEmptyHumanoidParts(PartDefinition root) {
        root.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.ZERO);
    }
}

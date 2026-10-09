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
import net.minecraft.world.entity.LivingEntity;

public final class BeltModel extends HumanoidModel<LivingEntity> {
    public static final ModelLayerLocation WARRIOR_LAYER_LOCATION = layer("warrior_belt");
    public static final ModelLayerLocation LITTLE_TAILORS_LAYER_LOCATION = layer("little_tailors_belt");
    public static final ModelLayerLocation VERDANT_SCALE_LAYER_LOCATION = layer("verdant_scale_belt");

    public BeltModel(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createWarriorBodyLayer() {
        MeshDefinition mesh = createMesh();
        PartDefinition belt = mesh.getRoot().getChild("body").addOrReplaceChild(
                "belt", beltCubes()
                        .texOffs(0, 10)
                        .addBox(-1.5F, 7.0F, -3.0F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.ZERO);
        belt.addOrReplaceChild("feather_2_r1", CubeListBuilder.create()
                        .texOffs(8, 12)
                        .addBox(-2.0F, -2.0F, -1.0F, 3.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
                        .texOffs(8, 10)
                        .addBox(3.0F, -2.0F, -1.0F, 3.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-2.0F, 10.0F, -2.0F, -0.2618F, 0.0F, 0.0F));
        return LayerDefinition.create(mesh, 32, 32);
    }

    public static LayerDefinition createLittleTailorsBodyLayer() {
        MeshDefinition mesh = createMesh();
        PartDefinition belt = mesh.getRoot().getChild("body").addOrReplaceChild(
                "belt", beltCubes(), PartPose.ZERO);
        belt.addOrReplaceChild("iron", CubeListBuilder.create()
                        .texOffs(0, 10)
                        .addBox(-2.0F, 7.0F, -3.0F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(4, 10)
                        .addBox(-1.0F, 7.0F, -3.0F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(10, 10)
                        .addBox(-1.0F, 9.0F, -3.0F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(4, 12)
                        .addBox(1.0F, 7.0F, -3.0F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.25F, 0.0F));
        return LayerDefinition.create(mesh, 32, 32);
    }

    public static LayerDefinition createVerdantScaleBodyLayer() {
        MeshDefinition mesh = createMesh();
        PartDefinition belt = mesh.getRoot().getChild("body").addOrReplaceChild(
                "belt", beltCubes(), PartPose.ZERO);
        belt.addOrReplaceChild("scale_3_r1", CubeListBuilder.create()
                        .texOffs(0, 14)
                        .addBox(-2.0F, -3.0F, -1.0F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.5F, 12.0F, -3.0F, -0.2618F, 0.0F, 0.0F));
        belt.addOrReplaceChild("scale_2_r1", CubeListBuilder.create()
                        .texOffs(8, 10)
                        .addBox(-1.5F, -1.5F, -0.5F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(3.5F, 10.4217F, -2.6947F, -0.2706F, -0.2527F, 0.0692F));
        belt.addOrReplaceChild("scale_1_r1", CubeListBuilder.create()
                        .texOffs(0, 10)
                        .addBox(-1.5F, -1.5F, -0.5F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-3.5F, 10.4217F, -2.6947F, -0.2706F, 0.2527F, -0.0692F));
        return LayerDefinition.create(mesh, 32, 32);
    }

    private static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.offset(-5.0F, 2.0F, 0.0F));
        root.addOrReplaceChild("left_arm", CubeListBuilder.create(), PartPose.offset(5.0F, 2.0F, 0.0F));
        root.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.offset(-1.9F, 12.0F, 0.0F));
        root.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.offset(1.9F, 12.0F, 0.0F));
        return mesh;
    }

    private static CubeListBuilder beltCubes() {
        return CubeListBuilder.create().texOffs(0, 0)
                .addBox(-4.0F, 6.0F, -2.0F, 8.0F, 6.0F, 4.0F, new CubeDeformation(0.3F));
    }

    private static ModelLayerLocation layer(String name) {
        return new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(ReliceraMod.MOD_ID, name), "main");
    }
}

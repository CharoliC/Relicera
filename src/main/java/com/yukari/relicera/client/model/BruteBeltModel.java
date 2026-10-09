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

public final class BruteBeltModel extends HumanoidModel<LivingEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(ReliceraMod.MOD_ID, "brute_belt"),
            "main"
    );

    public BruteBeltModel(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.offset(-5.0F, 2.0F, 0.0F));
        root.addOrReplaceChild("left_arm", CubeListBuilder.create(), PartPose.offset(5.0F, 2.0F, 0.0F));
        root.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.offset(-1.9F, 12.0F, 0.0F));
        root.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.offset(1.9F, 12.0F, 0.0F));

        PartDefinition body = root.addOrReplaceChild(
                "body", CubeListBuilder.create(), PartPose.ZERO);
        PartDefinition belt = body.addOrReplaceChild(
                "belt",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-4.0F, 6.0F, -2.0F, 8.0F, 6.0F, 4.0F,
                                new CubeDeformation(0.3F)),
                PartPose.ZERO
        );
        belt.addOrReplaceChild(
                "gold_medal",
                CubeListBuilder.create()
                        .texOffs(0, 10)
                        .addBox(-2.0F, 6.0F, -3.25F, 1.0F, 5.0F, 1.0F,
                                new CubeDeformation(0.0F))
                        .texOffs(4, 10)
                        .addBox(1.0F, 6.0F, -3.25F, 1.0F, 5.0F, 1.0F,
                                new CubeDeformation(0.0F))
                        .texOffs(8, 10)
                        .addBox(-1.0F, 6.0F, -3.25F, 2.0F, 1.0F, 1.0F,
                                new CubeDeformation(0.0F))
                        .texOffs(8, 12)
                        .addBox(-1.0F, 10.0F, -3.25F, 2.0F, 1.0F, 1.0F,
                                new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, 0.4F)
        );

        return LayerDefinition.create(mesh, 32, 32);
    }
}

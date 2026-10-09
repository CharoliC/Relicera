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

/** Blockbench-authored wearable model for the Devil's Bearskin. */
public final class DevilsBearskinModel extends HumanoidModel<LivingEntity> {
    private static final CubeDeformation FLAT_PART_INFLATION = new CubeDeformation(0.01F);

    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(ReliceraMod.MOD_ID, "devils_bearskin"),
            "main"
    );

    public DevilsBearskinModel(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition root = meshDefinition.getRoot();

        // HumanoidModel requires all six standard roots. Empty roots allow Forge
        // to copy the wearer's pose and armor-slot visibility onto this model.
        root.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.ZERO);

        PartDefinition body = root.addOrReplaceChild(
                "body",
                CubeListBuilder.create(),
                PartPose.offset(-0.0003F, 0.3556F, 0.0307F)
        );

        body.addOrReplaceChild(
                "collar",
                CubeListBuilder.create()
                        .texOffs(0, 39)
                        .addBox(-5.501F, 0.3112F, -2.8444F, 11.0F, 5.0F, 0.0F, FLAT_PART_INFLATION),
                PartPose.offsetAndRotation(0.0003F, 0.2131F, 0.3331F, -0.1745F, 0.0F, 0.0F)
        );

        PartDefinition hood = body.addOrReplaceChild(
                "hood",
                CubeListBuilder.create(),
                PartPose.offsetAndRotation(0.0003F, -0.0365F, 0.3386F, -0.4363F, 0.0F, 0.0F)
        );
        hood.addOrReplaceChild(
                "hood_cube",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-4.5F, -2.2416F, 2.3602F, 9.0F, 8.0F, 7.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.2618F, 0.0F, 0.0F)
        );

        PartDefinition mantle = body.addOrReplaceChild(
                "mantle",
                CubeListBuilder.create()
                        .texOffs(0, 15)
                        .addBox(-5.5F, -0.5585F, 3.1936F, 11.0F, 24.0F, 0.0F, FLAT_PART_INFLATION)
                        .texOffs(22, 15)
                        .addBox(-5.5F, -0.5585F, -2.8064F, 0.0F, 24.0F, 6.0F, FLAT_PART_INFLATION)
                        .texOffs(34, 0)
                        .addBox(5.5F, -0.5585F, -2.8064F, 0.0F, 24.0F, 6.0F, FLAT_PART_INFLATION)
                        .texOffs(34, 30)
                        .addBox(-5.5F, -0.5585F, -2.8064F, 11.0F, 0.0F, 6.0F, FLAT_PART_INFLATION),
                PartPose.offsetAndRotation(0.0003F, -0.0365F, 0.3386F, 0.2182F, 0.0F, 0.0F)
        );
        mantle.addOrReplaceChild(
                "back_outer",
                CubeListBuilder.create()
                        .texOffs(47, 7)
                        .addBox(-4.5F, -6.5F, 0.0F, 9.0F, 13.0F, 0.0F, FLAT_PART_INFLATION),
                PartPose.offsetAndRotation(0.0F, 15.9415F, 3.4936F, 0.0436F, 0.0F, 0.0F)
        );

        root.addOrReplaceChild(
                "left_arm",
                CubeListBuilder.create(),
                PartPose.offset(5.751F, 2.2082F, 0.0538F)
        );
        root.addOrReplaceChild(
                "right_arm",
                CubeListBuilder.create(),
                PartPose.offset(-5.751F, 2.2082F, 0.0538F)
        );

        return LayerDefinition.create(meshDefinition, 128, 128);
    }
}

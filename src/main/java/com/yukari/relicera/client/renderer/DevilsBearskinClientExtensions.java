package com.yukari.relicera.client.renderer;

import com.yukari.relicera.client.model.DevilsBearskinModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;

/** Client-only bridge supplying the custom humanoid armor model. */
public final class DevilsBearskinClientExtensions implements IClientItemExtensions {
    public static final DevilsBearskinClientExtensions INSTANCE = new DevilsBearskinClientExtensions();

    private DevilsBearskinModel model;

    private DevilsBearskinClientExtensions() {
    }

    @Override
    public @NotNull HumanoidModel<?> getHumanoidArmorModel(
            LivingEntity livingEntity,
            ItemStack itemStack,
            EquipmentSlot equipmentSlot,
            HumanoidModel<?> original
    ) {
        if (model == null) {
            model = new DevilsBearskinModel(
                    Minecraft.getInstance().getEntityModels().bakeLayer(DevilsBearskinModel.LAYER_LOCATION)
            );
        }
        return model;
    }
}

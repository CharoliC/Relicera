package com.yukari.relicera.client.toast;

import com.yukari.relicera.common.network.packet.CovenantToastPacket;
import com.yukari.relicera.registry.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.registries.ForgeRegistries;

public final class CovenantToast {
    private CovenantToast() {
    }

    public static void show(CovenantToastPacket.Kind kind, ResourceLocation entityTypeId) {
        EntityType<?> entityType = ForgeRegistries.ENTITY_TYPES.getValue(entityTypeId);
        if (entityType != null) {
            Component title = Component.translatable(kind == CovenantToastPacket.Kind.ESTABLISHED
                    ? "toast.relicera.covenant_established.title"
                    : "toast.relicera.covenant_broken.title");
            Component description = kind == CovenantToastPacket.Kind.ESTABLISHED
                    ? Component.translatable("toast.relicera.covenant_established.description")
                    : Component.translatable("toast.relicera.covenant_broken.description",
                            entityType.getDescription());
            ReliceraToast.show(title, description, ModItems.BAROMS_COVENANT_STONE.get().getDefaultInstance());
        }
    }
}

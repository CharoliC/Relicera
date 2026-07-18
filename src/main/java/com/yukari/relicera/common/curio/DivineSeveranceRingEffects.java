package com.yukari.relicera.common.curio;

import com.yukari.relicera.config.ModCommonConfig;
import com.yukari.relicera.registry.ModItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.registries.ForgeRegistries;
import top.theillusivec4.curios.api.CuriosApi;

public final class DivineSeveranceRingEffects {
    private DivineSeveranceRingEffects() {
    }

    public static boolean isEquipped(LivingEntity entity) {
        return CuriosApi.getCuriosInventory(entity)
                .resolve()
                .map(handler -> handler.isEquipped(ModItems.DIVINE_SEVERANCE_RING.get()))
                .orElse(false);
    }

    public static void addGlowingUndeadHeadDrop(LivingDropsEvent event) {
        if (!(event.getEntity().level() instanceof ServerLevel level)
                || !event.getEntity().hasEffect(MobEffects.GLOWING)
                || event.getEntity().getMobType() != MobType.UNDEAD
                || !(event.getSource().getEntity() instanceof LivingEntity killer)
                || !isEquipped(killer)) {
            return;
        }

        DropEntry dropEntry = findDropEntry(event.getEntity().getType());
        if (dropEntry == null) {
            return;
        }

        event.getDrops().add(new ItemEntity(
                level,
                event.getEntity().getX(),
                event.getEntity().getY() + 0.5D,
                event.getEntity().getZ(),
                new ItemStack(dropEntry.item())
        ));
    }

    private static DropEntry findDropEntry(EntityType<?> entityType) {
        ResourceLocation entityId = ForgeRegistries.ENTITY_TYPES.getKey(entityType);
        if (entityId == null) {
            return null;
        }

        for (String entry : ModCommonConfig.DIVINE_SEVERANCE_RING_HEAD_DROPS.get()) {
            DropEntry parsed = parseDropEntry(entry);
            if (parsed != null && parsed.entityId().equals(entityId)) {
                return parsed;
            }
        }
        return null;
    }

    private static DropEntry parseDropEntry(String entry) {
        String[] parts = entry.split("\\|");
        if (parts.length != 2) {
            return null;
        }

        ResourceLocation entityId = ResourceLocation.tryParse(parts[0].trim());
        ResourceLocation itemId = ResourceLocation.tryParse(parts[1].trim());
        if (entityId == null || itemId == null) {
            return null;
        }

        Item item = ForgeRegistries.ITEMS.getValue(itemId);
        if (item == null || !ForgeRegistries.ENTITY_TYPES.containsKey(entityId)) {
            return null;
        }

        return new DropEntry(entityId, item);
    }

    private record DropEntry(ResourceLocation entityId, Item item) {
    }
}

package com.yukari.relicera.common.curio;

import com.yukari.relicera.config.ModCommonConfig;
import com.yukari.relicera.registry.ModItems;
import com.yukari.relicera.registry.ModParticleTypes;
import java.util.ArrayList;
import java.util.List;
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
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.registries.ForgeRegistries;
import top.theillusivec4.curios.api.CuriosApi;

public final class DivineSeveranceRingEffects {
    private static final List<PendingJudgment> PENDING_JUDGMENTS = new ArrayList<>();

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

        ItemEntity head = new ItemEntity(
                level,
                event.getEntity().getX(),
                event.getEntity().getY() + 0.5D,
                event.getEntity().getZ(),
                new ItemStack(dropEntry.item())
        );
        event.getDrops().add(head);
        PENDING_JUDGMENTS.add(new PendingJudgment(event, head));
    }

    public static void processPendingJudgments(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || PENDING_JUDGMENTS.isEmpty()) {
            return;
        }
        List<PendingJudgment> judgments = new ArrayList<>(PENDING_JUDGMENTS);
        PENDING_JUDGMENTS.clear();
        for (PendingJudgment judgment : judgments) {
            LivingEntity victim = judgment.event().getEntity();
            if (!judgment.event().isCanceled() && judgment.event().getDrops().contains(judgment.head())
                    && !judgment.head().getItem().isEmpty() && victim.level() instanceof ServerLevel level) {
                level.sendParticles(ModParticleTypes.HOLY_LIGHT.get(), victim.getX(),
                        victim.getY() + victim.getBbHeight() * 0.65D, victim.getZ(),
                        1, 0.0D, 0.0D, 0.0D, 0.0D);
            }
        }
    }

    public static void clearPendingJudgments() {
        PENDING_JUDGMENTS.clear();
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

    private record PendingJudgment(LivingDropsEvent event, ItemEntity head) {
    }
}

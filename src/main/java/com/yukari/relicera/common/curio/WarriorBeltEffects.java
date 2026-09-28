package com.yukari.relicera.common.curio;

import com.yukari.relicera.config.ModCommonConfig;
import com.yukari.relicera.registry.ModItems;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.UUID;

public final class WarriorBeltEffects {
    private static final UUID KNOCKBACK_RESISTANCE_MODIFIER_ID =
            UUID.fromString("62cba150-d021-4143-a3b5-3e40e314aa65");
    private static final UUID MOVEMENT_SPEED_MODIFIER_ID =
            UUID.fromString("0dd3ee98-05e6-4808-a2ba-5fd9a644c024");
    private static final String KNOCKBACK_RESISTANCE_MODIFIER_NAME =
            "Relicera Belt of Warrior knockback resistance";
    private static final String MOVEMENT_SPEED_MODIFIER_NAME =
            "Relicera Belt of Warrior movement speed";

    private WarriorBeltEffects() {
    }

    public static boolean isEquipped(LivingEntity entity) {
        return CuriosApi.getCuriosInventory(entity)
                .resolve()
                .map(handler -> handler.isEquipped(ModItems.WARRIOR_BELT.get()))
                .orElse(false);
    }

    public static void increaseVanillaCriticalDamage(CriticalHitEvent event) {
        if (!event.isVanillaCritical() || !isEquipped(event.getEntity())) {
            return;
        }

        event.setDamageModifier(event.getDamageModifier()
                + ModCommonConfig.WARRIOR_BELT_CRITICAL_DAMAGE_BONUS.get().floatValue());
    }

    public static void tickAttributes(LivingEntity entity) {
        if (entity.level().isClientSide()) {
            return;
        }

        boolean equipped = isEquipped(entity);
        applyAttribute(
                entity,
                Attributes.KNOCKBACK_RESISTANCE,
                KNOCKBACK_RESISTANCE_MODIFIER_ID,
                KNOCKBACK_RESISTANCE_MODIFIER_NAME,
                equipped ? ModCommonConfig.WARRIOR_BELT_KNOCKBACK_RESISTANCE.get() : 0.0D,
                AttributeModifier.Operation.ADDITION
        );
        applyAttribute(
                entity,
                Attributes.MOVEMENT_SPEED,
                MOVEMENT_SPEED_MODIFIER_ID,
                MOVEMENT_SPEED_MODIFIER_NAME,
                equipped ? ModCommonConfig.WARRIOR_BELT_MOVEMENT_SPEED_BONUS.get() : 0.0D,
                AttributeModifier.Operation.MULTIPLY_TOTAL
        );
    }

    private static void applyAttribute(
            LivingEntity entity,
            Attribute attribute,
            UUID id,
            String name,
            double amount,
            AttributeModifier.Operation operation
    ) {
        AttributeInstance instance = entity.getAttribute(attribute);
        if (instance == null) {
            return;
        }

        AttributeModifier existing = instance.getModifier(id);
        if (amount <= 0.0D) {
            if (existing != null) {
                instance.removeModifier(id);
            }
            return;
        }

        if (existing != null
                && existing.getOperation() == operation
                && Math.abs(existing.getAmount() - amount) < 0.0001D) {
            return;
        }

        if (existing != null) {
            instance.removeModifier(id);
        }
        instance.addTransientModifier(new AttributeModifier(id, name, amount, operation));
    }
}

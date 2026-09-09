package com.yukari.relicera.common.curio;

import com.yukari.relicera.config.ModCommonConfig;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public final class IllagerMedalAxeEffects {
    private static final UUID ATTACK_SPEED_MODIFIER_ID = UUID.fromString("d4f5fd47-983e-449c-a067-3a9fdb7bc6de");
    private static final String ATTACK_SPEED_MODIFIER_NAME = "Relicera illager medal attack speed";
    private static final int SLOWNESS_DURATION_TICKS = 100;
    private static final int SLOWNESS_AMPLIFIER = 2;

    private IllagerMedalAxeEffects() {
    }

    public static void tickAttackSpeed(LivingEntity entity) {
        if (entity.level().isClientSide()) {
            return;
        }

        AttributeInstance attackSpeed = entity.getAttribute(Attributes.ATTACK_SPEED);
        if (attackSpeed == null) {
            return;
        }

        double amount = getAttackSpeedBonus(entity);
        boolean active = amount > 0.0D && entity.getMainHandItem().is(ItemTags.AXES);
        AttributeModifier existing = attackSpeed.getModifier(ATTACK_SPEED_MODIFIER_ID);
        if (!active) {
            if (existing != null) {
                attackSpeed.removeModifier(ATTACK_SPEED_MODIFIER_ID);
            }
            return;
        }

        if (existing != null
                && existing.getOperation() == AttributeModifier.Operation.MULTIPLY_TOTAL
                && Math.abs(existing.getAmount() - amount) < 0.0001D) {
            return;
        }

        if (existing != null) {
            attackSpeed.removeModifier(ATTACK_SPEED_MODIFIER_ID);
        }
        attackSpeed.addTransientModifier(new AttributeModifier(
                ATTACK_SPEED_MODIFIER_ID,
                ATTACK_SPEED_MODIFIER_NAME,
                amount,
                AttributeModifier.Operation.MULTIPLY_TOTAL
        ));
    }

    public static void applyAxeHitEffects(LivingDamageEvent event) {
        LivingEntity attacker = getEquippedAxeAttacker(event);
        if (attacker == null) {
            return;
        }

        event.getEntity().addEffect(
                new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, SLOWNESS_DURATION_TICKS, SLOWNESS_AMPLIFIER),
                attacker
        );
        if (VindicatorsMedalEffects.isEquipped(attacker)) {
            VindicatorsMedalEffects.dropRandomVillagerTrade(event.getEntity());
        }
    }

    private static double getAttackSpeedBonus(LivingEntity entity) {
        if (VindicatorsMedalEffects.isEquipped(entity)) {
            return ModCommonConfig.VINDICATORS_MEDAL_AXE_ATTACK_SPEED_BONUS.get();
        }
        return 0.0D;
    }

    @Nullable
    private static LivingEntity getEquippedAxeAttacker(LivingDamageEvent event) {
        if (event.getEntity().level().isClientSide()
                || event.getAmount() <= 0.0F
                || !(event.getSource().getEntity() instanceof LivingEntity attacker)
                || event.getSource().getDirectEntity() != attacker
                || !attacker.getMainHandItem().is(ItemTags.AXES)
                || !VindicatorsMedalEffects.isEquipped(attacker)) {
            return null;
        }
        return attacker;
    }
}

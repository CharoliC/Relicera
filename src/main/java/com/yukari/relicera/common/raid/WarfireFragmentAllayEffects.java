package com.yukari.relicera.common.raid;

import com.yukari.relicera.config.ModCommonConfig;
import com.yukari.relicera.registry.ModItems;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

public final class WarfireFragmentAllayEffects {
    private static final int AURA_REFRESH_INTERVAL_TICKS = 40;
    private static final int AURA_EFFECT_DURATION_TICKS = 240;

    private WarfireFragmentAllayEffects() {
    }

    public static void tickAllayAura(LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof Allay allay)
                || !(allay.level() instanceof ServerLevel level)
                || allay.tickCount % AURA_REFRESH_INTERVAL_TICKS != 0
                || !isHoldingWarfireFragment(allay)) {
            return;
        }

        double range = ModCommonConfig.WARFIRE_FRAGMENT_ALLAY_AURA_RANGE.get();
        if (range <= 0.0D) {
            return;
        }

        double rangeSqr = range * range;
        level.getEntitiesOfClass(
                LivingEntity.class,
                allay.getBoundingBox().inflate(range),
                target -> target.distanceToSqr(allay) <= rangeSqr && isAuraTarget(target)
        ).forEach(WarfireFragmentAllayEffects::applyAuraEffects);
    }

    public static void preventAllayDamage(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof Allay allay) || !isHoldingWarfireFragment(allay)) {
            return;
        }

        if (isPreventedDamage(event.getSource())) {
            event.setCanceled(true);
        }
    }

    public static void preventAllayAttack(LivingAttackEvent event) {
        if (!(event.getEntity() instanceof Allay allay) || !isHoldingWarfireFragment(allay)) {
            return;
        }

        if (isPreventedDamage(event.getSource())) {
            event.setCanceled(true);
        }
    }

    private static boolean isHoldingWarfireFragment(Allay allay) {
        return allay.getMainHandItem().is(ModItems.WARFIRE_FRAGMENT.get());
    }

    private static boolean isAuraTarget(LivingEntity entity) {
        return entity instanceof Player
                || entity instanceof IronGolem
                || isOwnedByPlayer(entity);
    }

    private static boolean isOwnedByPlayer(LivingEntity entity) {
        return entity instanceof OwnableEntity ownableEntity && ownableEntity.getOwnerUUID() != null;
    }

    private static void applyAuraEffects(LivingEntity target) {
        target.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, AURA_EFFECT_DURATION_TICKS, 1, true, true, true));
        target.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, AURA_EFFECT_DURATION_TICKS, 0, true, true, true));
    }

    private static boolean isPlayerOrOwnedUnitDamage(DamageSource source) {
        return source.getEntity() instanceof Player
                || source.getEntity() instanceof LivingEntity livingEntity && isOwnedByPlayer(livingEntity);
    }

    private static boolean isFireOrLavaDamage(DamageSource source) {
        return source.is(DamageTypes.IN_FIRE)
                || source.is(DamageTypes.ON_FIRE)
                || source.is(DamageTypes.LAVA);
    }

    private static boolean isPreventedDamage(DamageSource source) {
        return isFireOrLavaDamage(source) || isPlayerOrOwnedUnitDamage(source);
    }
}

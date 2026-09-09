package com.yukari.relicera.common.curio;

import com.yukari.relicera.config.ModCommonConfig;
import com.yukari.relicera.registry.ModItems;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import top.theillusivec4.curios.api.CuriosApi;

public final class TurncoatsMedalEffects {
    private static final int FULL_BONUS_MIN_DURATION_TICKS = 90 * 20;
    private static final double REDUCED_BONUS_MULTIPLIER = 1.0D / 5.0D;

    private TurncoatsMedalEffects() {
    }

    public static boolean isEquipped(LivingEntity entity) {
        return CuriosApi.getCuriosInventory(entity)
                .resolve()
                .map(handler -> handler.isEquipped(ModItems.TURNCOATS_MEDAL.get()))
                .orElse(false);
    }

    public static double getHeroDamageBonus(LivingEntity entity) {
        MobEffectInstance hero = entity.getEffect(MobEffects.HERO_OF_THE_VILLAGE);
        return hero == null ? 0.0D : ModCommonConfig.TURNCOATS_MEDAL_HERO_DAMAGE_BONUS_PER_LEVEL.get()
                * (hero.getAmplifier() + 1) * getDurationMultiplier(hero);
    }

    public static double getBadOmenLifeSteal(LivingEntity entity) {
        MobEffectInstance badOmen = entity.getEffect(MobEffects.BAD_OMEN);
        return badOmen == null ? 0.0D : ModCommonConfig.TURNCOATS_MEDAL_BAD_OMEN_LIFE_STEAL_PER_LEVEL.get()
                * (badOmen.getAmplifier() + 1) * getDurationMultiplier(badOmen);
    }

    public static void applyHeroDamageBonus(LivingHurtEvent event) {
        if (event.getAmount() <= 0.0F
                || !(event.getSource().getEntity() instanceof LivingEntity attacker)
                || attacker == event.getEntity()
                || !isEquipped(attacker)) {
            return;
        }

        double bonus = getHeroDamageBonus(attacker);
        if (bonus > 0.0D) {
            event.setAmount((float) (event.getAmount() * (1.0D + bonus)));
        }
    }

    public static void applyBadOmenLifeSteal(LivingDamageEvent event) {
        if (event.getEntity().level().isClientSide()
                || event.getAmount() <= 0.0F
                || !(event.getSource().getEntity() instanceof LivingEntity attacker)
                || attacker == event.getEntity()
                || !isEquipped(attacker)) {
            return;
        }

        double lifeSteal = getBadOmenLifeSteal(attacker);
        if (lifeSteal <= 0.0D) {
            return;
        }

        float actualHealthDamage = Math.min(event.getAmount(), event.getEntity().getHealth());
        attacker.heal((float) (actualHealthDamage * lifeSteal));
    }

    private static double getDurationMultiplier(MobEffectInstance effect) {
        return effect.isInfiniteDuration() || effect.getDuration() < FULL_BONUS_MIN_DURATION_TICKS
                ? REDUCED_BONUS_MULTIPLIER
                : 1.0D;
    }
}

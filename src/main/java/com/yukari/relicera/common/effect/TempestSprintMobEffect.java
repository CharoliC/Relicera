package com.yukari.relicera.common.effect;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public class TempestSprintMobEffect extends MobEffect {
    private static final int REGENERATION_III_HEAL_INTERVAL_TICKS = 12;

    public TempestSprintMobEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x6CCBFF);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity instanceof Horse && entity.getHealth() < entity.getMaxHealth()) {
            entity.heal(1.0F);
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration % REGENERATION_III_HEAL_INTERVAL_TICKS == 0;
    }
}

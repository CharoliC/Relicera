package com.yukari.relicera.common.effect.furnaceofwar;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public final class FurnaceOfWarMobEffect extends MobEffect {
    public FurnaceOfWarMobEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xA72A02);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        FurnaceOfWarEffects.burnNearbyProjectiles(entity);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }
}

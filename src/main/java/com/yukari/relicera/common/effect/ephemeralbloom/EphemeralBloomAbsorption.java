package com.yukari.relicera.common.effect.ephemeralbloom;

import com.yukari.relicera.config.ModCommonConfig;
import net.minecraft.world.entity.LivingEntity;

public final class EphemeralBloomAbsorption {
    private static final float AMOUNT_PER_INTERVAL = 2.0F;

    private EphemeralBloomAbsorption() {
    }

    public static int intervalTicks() {
        return ModCommonConfig.EPHEMERAL_BLOOM_ABSORPTION_INTERVAL_TICKS.get();
    }

    public static void grant(LivingEntity entity) {
        float maxAbsorption = ModCommonConfig.EPHEMERAL_BLOOM_MAX_ABSORPTION.get();
        float currentAbsorption = entity.getAbsorptionAmount();
        if (currentAbsorption < maxAbsorption) {
            entity.setAbsorptionAmount(Math.min(maxAbsorption, currentAbsorption + AMOUNT_PER_INTERVAL));
        }
    }
}

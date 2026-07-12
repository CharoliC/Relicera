package com.yukari.relicera.common.effect;

import com.yukari.relicera.config.ModCommonConfig;
import com.yukari.relicera.registry.ModEffects;
import com.yukari.relicera.registry.ModParticleTypes;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

public final class IluthiasBlessingEffects {
    private IluthiasBlessingEffects() {
    }

    public static boolean preventDamage(LivingHurtEvent event) {
        LivingEntity entity = event.getEntity();
        if (!entity.hasEffect(ModEffects.ILUTHIAS_BLESSING.get())) {
            return false;
        }

        float amount = event.getAmount();
        if (amount > 0.0F) {
            spawnGoldHeartParticles(entity);
            float maxAbsorption = ModCommonConfig.ILUTHIAS_BLESSING_MAX_ABSORPTION.get();
            float currentAbsorption = entity.getAbsorptionAmount();
            if (currentAbsorption < maxAbsorption) {
                entity.setAbsorptionAmount(Math.min(maxAbsorption, currentAbsorption + amount));
            }
        }
        event.setCanceled(true);
        return true;
    }

    private static void spawnGoldHeartParticles(LivingEntity entity) {
        if (!(entity.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        RandomSource random = serverLevel.getRandom();
        double x = entity.getX();
        double y = entity.getY(0.75D);
        double z = entity.getZ();
        double width = Math.max(0.35D, entity.getBbWidth() * 0.35D);
        double height = Math.max(0.25D, entity.getBbHeight() * 0.18D);
        serverLevel.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, x, y, z, 8, width, height, width, 0.05D);
        for (int i = 0; i < 6; i++) {
            spawnGoldHeart(serverLevel, ModParticleTypes.GOLDHEART_0.get(), entity, random, 0.015D);
        }
        for (int i = 0; i < 4; i++) {
            spawnGoldHeart(serverLevel, ModParticleTypes.GOLDHEART_1.get(), entity, random, 0.02D);
        }
        spawnGoldHeart(serverLevel, ModParticleTypes.GOLDHEART_2.get(), entity, random, 0.025D);
    }

    private static void spawnGoldHeart(ServerLevel serverLevel, net.minecraft.core.particles.SimpleParticleType particle, LivingEntity entity, RandomSource random, double upwardSpeed) {
        double radius = Math.max(0.25D, entity.getBbWidth() * 0.45D);
        double x = entity.getX() + (random.nextDouble() - 0.5D) * radius * 2.0D;
        double y = entity.getY(0.6D + random.nextDouble() * 0.35D);
        double z = entity.getZ() + (random.nextDouble() - 0.5D) * radius * 2.0D;
        double xSpeed = (random.nextDouble() - 0.5D) * 0.04D;
        double zSpeed = (random.nextDouble() - 0.5D) * 0.04D;
        serverLevel.sendParticles(particle, x, y, z, 0, xSpeed, upwardSpeed + random.nextDouble() * 0.03D, zSpeed, 1.0D);
    }
}

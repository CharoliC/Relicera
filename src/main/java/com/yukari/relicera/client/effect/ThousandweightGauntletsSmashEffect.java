package com.yukari.relicera.client.effect;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public final class ThousandweightGauntletsSmashEffect {
    private static final int CENTRAL_PARTICLE_COUNT = 80;
    private static final int RING_PARTICLE_COUNT = 160;
    private static final double PARTICLE_RING_RADIUS = 3.5D;
    private static final LiftedBlockRingEffect.RadialPulse GROUND_PULSE =
            new LiftedBlockRingEffect.RadialPulse(
                    2.75D,
                    0.55D,
                    0.20D,
                    4,
                    0.84F
            );

    private ThousandweightGauntletsSmashEffect() {
    }

    public static void triggerSmash(
            double x,
            double y,
            double z,
            int blockStateId,
            boolean windBurst
    ) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }

        if (windBurst) {
            level.addParticle(
                    ParticleTypes.EXPLOSION_EMITTER,
                    x,
                    y,
                    z,
                    0.0D,
                    0.0D,
                    0.0D
            );
        }

        LiftedBlockRingEffect.spawnRadialPulse(
                x,
                y,
                z,
                GROUND_PULSE
        );

        BlockState state = Block.stateById(blockStateId);
        if (state.isAir()) {
            spawnAirImpactParticles(level, x, y, z);
            return;
        }

        BlockParticleOption particle = new BlockParticleOption(ParticleTypes.BLOCK, state);
        spawnCentralParticles(level, x, y, z, particle);
        spawnRingParticles(level, x, y, z, particle);
    }

    private static void spawnCentralParticles(ClientLevel level, double x, double y, double z,
                                              BlockParticleOption particle) {
        RandomSource random = level.random;
        for (int index = 0; index < CENTRAL_PARTICLE_COUNT; index++) {
            double offsetX = random.nextGaussian() * 0.45D;
            double offsetZ = random.nextGaussian() * 0.45D;
            level.addParticle(
                    particle,
                    x + offsetX,
                    y + random.nextDouble() * 0.3D,
                    z + offsetZ,
                    offsetX * 0.12D,
                    0.08D + random.nextDouble() * 0.22D,
                    offsetZ * 0.12D
            );
        }
    }

    private static void spawnRingParticles(ClientLevel level, double x, double y, double z,
                                           BlockParticleOption particle) {
        RandomSource random = level.random;
        for (int index = 0; index < RING_PARTICLE_COUNT; index++) {
            double angle = Math.PI * 2.0D * index / RING_PARTICLE_COUNT + random.nextGaussian() * 0.025D;
            double radius = PARTICLE_RING_RADIUS + random.nextGaussian() * 0.18D;
            double directionX = Math.cos(angle);
            double directionZ = Math.sin(angle);
            level.addParticle(
                    particle,
                    x + directionX * radius,
                    y + random.nextDouble() * 0.2D,
                    z + directionZ * radius,
                    directionX * (0.04D + random.nextDouble() * 0.08D),
                    0.04D + random.nextDouble() * 0.12D,
                    directionZ * (0.04D + random.nextDouble() * 0.08D)
            );
        }
    }

    private static void spawnAirImpactParticles(ClientLevel level, double x, double y, double z) {
        RandomSource random = level.random;
        for (int index = 0; index < CENTRAL_PARTICLE_COUNT; index++) {
            double offsetX = random.nextGaussian() * 0.55D;
            double offsetZ = random.nextGaussian() * 0.55D;
            level.addParticle(
                    ParticleTypes.POOF,
                    x + offsetX,
                    y + random.nextDouble() * 0.3D,
                    z + offsetZ,
                    offsetX * 0.1D,
                    0.04D + random.nextDouble() * 0.16D,
                    offsetZ * 0.1D
            );
        }
    }
}

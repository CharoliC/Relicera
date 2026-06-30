package com.yukari.relicera.common.recipe;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;

public enum RelicRepairParticleStyle {
    NONE {
        @Override
        public void spawnActive(ServerLevel level, BlockPos pos, int progress) {
        }

        @Override
        public void spawnComplete(ServerLevel level, BlockPos pos) {
        }
    },
    SOLAR_FURNACE {
        @Override
        public void spawnActive(ServerLevel level, BlockPos pos, int progress) {
            if (progress % ACTIVE_PARTICLE_INTERVAL != 0) {
                return;
            }

            level.sendParticles(
                    ParticleTypes.FLAME,
                    particleX(pos),
                    particleY(pos),
                    particleZ(pos),
                    ACTIVE_FLAME_COUNT,
                    ACTIVE_PARTICLE_SPREAD,
                    ACTIVE_PARTICLE_SPREAD * 0.5D,
                    ACTIVE_PARTICLE_SPREAD,
                    ACTIVE_PARTICLE_SPEED
            );
        }

        @Override
        public void spawnComplete(ServerLevel level, BlockPos pos) {
            double x = particleX(pos);
            double y = particleY(pos);
            double z = particleZ(pos);
            level.sendParticles(ParticleTypes.FLAME, x, y, z, COMPLETE_FLAME_COUNT, COMPLETE_PARTICLE_SPREAD, COMPLETE_PARTICLE_SPREAD, COMPLETE_PARTICLE_SPREAD, COMPLETE_PARTICLE_SPEED);
            level.sendParticles(ParticleTypes.LAVA, x, y, z, COMPLETE_LAVA_COUNT, COMPLETE_PARTICLE_SPREAD * 0.65D, COMPLETE_PARTICLE_SPREAD * 0.4D, COMPLETE_PARTICLE_SPREAD * 0.65D, COMPLETE_PARTICLE_SPEED);
        }
    },
    LIFE_CHALICE {
        @Override
        public void spawnActive(ServerLevel level, BlockPos pos, int progress) {
            if (progress % ACTIVE_PARTICLE_INTERVAL != 0) {
                return;
            }

            level.sendParticles(
                    ParticleTypes.GLOW,
                    particleX(pos),
                    particleY(pos),
                    particleZ(pos),
                    ACTIVE_GLOW_COUNT,
                    ACTIVE_PARTICLE_SPREAD,
                    ACTIVE_PARTICLE_SPREAD * 0.5D,
                    ACTIVE_PARTICLE_SPREAD,
                    ACTIVE_PARTICLE_SPEED
            );
        }

        @Override
        public void spawnComplete(ServerLevel level, BlockPos pos) {
            double x = particleX(pos);
            double y = particleY(pos);
            double z = particleZ(pos);
            level.sendParticles(ParticleTypes.GLOW, x, y, z, COMPLETE_GLOW_COUNT, COMPLETE_PARTICLE_SPREAD, COMPLETE_PARTICLE_SPREAD, COMPLETE_PARTICLE_SPREAD, COMPLETE_PARTICLE_SPEED);
            level.sendParticles(ParticleTypes.FALLING_SPORE_BLOSSOM, x, y, z, COMPLETE_SPORE_BLOSSOM_COUNT, COMPLETE_PARTICLE_SPREAD * 0.65D, COMPLETE_PARTICLE_SPREAD * 0.4D, COMPLETE_PARTICLE_SPREAD * 0.65D, COMPLETE_PARTICLE_SPEED);
        }
    };

    private static final int ACTIVE_PARTICLE_INTERVAL = 4;
    private static final int ACTIVE_FLAME_COUNT = 2;
    private static final int ACTIVE_GLOW_COUNT = 2;
    private static final int COMPLETE_FLAME_COUNT = 20;
    private static final int COMPLETE_LAVA_COUNT = 8;
    private static final int COMPLETE_GLOW_COUNT = 20;
    private static final int COMPLETE_SPORE_BLOSSOM_COUNT = 8;
    private static final double PARTICLE_Y_OFFSET = 0.78D;
    private static final double ACTIVE_PARTICLE_SPREAD = 0.14D;
    private static final double COMPLETE_PARTICLE_SPREAD = 0.22D;
    private static final double ACTIVE_PARTICLE_SPEED = 0.01D;
    private static final double COMPLETE_PARTICLE_SPEED = 0.02D;

    public abstract void spawnActive(ServerLevel level, BlockPos pos, int progress);

    public abstract void spawnComplete(ServerLevel level, BlockPos pos);

    private static double particleX(BlockPos pos) {
        return pos.getX() + 0.5D;
    }

    private static double particleY(BlockPos pos) {
        return pos.getY() + PARTICLE_Y_OFFSET;
    }

    private static double particleZ(BlockPos pos) {
        return pos.getZ() + 0.5D;
    }
}

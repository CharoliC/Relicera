package com.yukari.relicera.common.recipe;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;

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
    },
    NEREIAS_CROWN {
        @Override
        public void spawnActive(ServerLevel level, BlockPos pos, int progress) {
            if (progress % NEREIA_ACTIVE_PARTICLE_INTERVAL != 0) {
                return;
            }

            spawnConvergingNautilus(level, pos, ACTIVE_NAUTILUS_COUNT, NEREIA_ACTIVE_RADIUS);
        }

        @Override
        public void spawnComplete(ServerLevel level, BlockPos pos) {
            spawnSplashBurst(level, pos);
        }
    };

    private static final int ACTIVE_PARTICLE_INTERVAL = 4;
    private static final int ACTIVE_FLAME_COUNT = 2;
    private static final int ACTIVE_GLOW_COUNT = 2;
    private static final int COMPLETE_FLAME_COUNT = 20;
    private static final int COMPLETE_LAVA_COUNT = 8;
    private static final int COMPLETE_GLOW_COUNT = 20;
    private static final int COMPLETE_SPORE_BLOSSOM_COUNT = 8;
    private static final int NEREIA_ACTIVE_PARTICLE_INTERVAL = 2;
    private static final int ACTIVE_NAUTILUS_COUNT = 3;
    private static final int COMPLETE_SPLASH_COUNT = 44;
    private static final double PARTICLE_Y_OFFSET = 0.78D;
    private static final double DISPLAY_ITEM_Y_OFFSET = 1.15D;
    private static final double ACTIVE_PARTICLE_SPREAD = 0.14D;
    private static final double COMPLETE_PARTICLE_SPREAD = 0.22D;
    private static final double ACTIVE_PARTICLE_SPEED = 0.01D;
    private static final double COMPLETE_PARTICLE_SPEED = 0.02D;
    private static final double NEREIA_ACTIVE_RADIUS = 1.05D;
    private static final double NEREIA_VERTICAL_SPREAD = 0.16D;
    private static final double NAUTILUS_FALL_COMPENSATION = 1.2D;

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

    // Nautilus uses x/y/z as the target base and xSpeed/ySpeed/zSpeed as the starting offset.
    private static void spawnConvergingNautilus(ServerLevel level, BlockPos pos, int count, double radius) {
        RandomSource random = level.getRandom();
        double centerX = particleX(pos);
        double centerY = displayItemY(pos) + NAUTILUS_FALL_COMPENSATION;
        double centerZ = particleZ(pos);
        for (int i = 0; i < count; i++) {
            double angle = random.nextDouble() * Math.PI * 2.0D;
            double xOffset = Math.cos(angle) * radius * (0.85D + random.nextDouble() * 0.35D);
            double yOffset = -NAUTILUS_FALL_COMPENSATION + (random.nextDouble() - 0.5D) * NEREIA_VERTICAL_SPREAD;
            double zOffset = Math.sin(angle) * radius * (0.85D + random.nextDouble() * 0.35D);
            level.sendParticles(ParticleTypes.NAUTILUS, centerX, centerY, centerZ, 0, xOffset, yOffset, zOffset, 1.0D);
        }
    }

    private static void spawnSplashBurst(ServerLevel level, BlockPos pos) {
        RandomSource random = level.getRandom();
        double centerX = particleX(pos);
        double centerY = displayItemY(pos);
        double centerZ = particleZ(pos);
        for (int i = 0; i < COMPLETE_SPLASH_COUNT; i++) {
            double angle = random.nextDouble() * Math.PI * 2.0D;
            double startRadius = random.nextDouble() * 0.18D;
            double speed = 0.08D + random.nextDouble() * 0.16D;
            double x = centerX + Math.cos(angle) * startRadius;
            double y = centerY + (random.nextDouble() - 0.5D) * 0.12D;
            double z = centerZ + Math.sin(angle) * startRadius;
            double ySpeed = 0.04D + random.nextDouble() * 0.12D;
            level.sendParticles(ParticleTypes.SPLASH, x, y, z, 0, Math.cos(angle) * speed, ySpeed, Math.sin(angle) * speed, 1.0D);
        }
    }

    private static double displayItemY(BlockPos pos) {
        return pos.getY() + DISPLAY_ITEM_Y_OFFSET;
    }
}

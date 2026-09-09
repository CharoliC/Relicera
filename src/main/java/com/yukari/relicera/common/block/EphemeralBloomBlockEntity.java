package com.yukari.relicera.common.block;

import com.yukari.relicera.common.effect.ephemeralbloom.EphemeralBloomAbsorption;
import com.yukari.relicera.registry.ModBlockEntities;
import com.yukari.relicera.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class EphemeralBloomBlockEntity extends BlockEntity {
    private static final String EXPOSURE_TICKS_TAG = "ExposureTicks";
    private static final String LAST_TICK_TIME_TAG = "LastTickTime";
    private static final int CLEAR_DAYLIGHT_DURATION_TICKS = 15 * 20;
    private static final int AMBIENT_SMOKE_INTERVAL = 5;
    private static final int AMBIENT_END_ROD_AVERAGE_INTERVAL = 40;
    private static final double ABSORPTION_AURA_RANGE = 8.0D;
    private static final double ABSORPTION_AURA_RANGE_SQUARED = ABSORPTION_AURA_RANGE * ABSORPTION_AURA_RANGE;

    private int exposureTicks;
    private long lastTickTime = Long.MIN_VALUE;

    public EphemeralBloomBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.EPHEMERAL_BLOOM.get(), pos, state);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        exposureTicks = Math.max(0, tag.getInt(EXPOSURE_TICKS_TAG));
        lastTickTime = tag.contains(LAST_TICK_TIME_TAG) ? tag.getLong(LAST_TICK_TIME_TAG) : Long.MIN_VALUE;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt(EXPOSURE_TICKS_TAG, exposureTicks);
        if (lastTickTime != Long.MIN_VALUE) {
            tag.putLong(LAST_TICK_TIME_TAG, lastTickTime);
        }
    }

    public static void tick(Level level, BlockPos pos, BlockState state, EphemeralBloomBlockEntity blockEntity) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        long gameTime = serverLevel.getGameTime();
        if (blockEntity.lastTickTime != Long.MIN_VALUE && blockEntity.lastTickTime + 1L != gameTime) {
            blockEntity.exposureTicks = 0;
        }
        blockEntity.lastTickTime = gameTime;

        if (gameTime % EphemeralBloomAbsorption.intervalTicks() == 0L) {
            applyAbsorptionAura(serverLevel, pos);
        }

        if (!isInWitheringEnvironment(serverLevel, pos)) {
            if (blockEntity.exposureTicks != 0) {
                blockEntity.exposureTicks = 0;
                blockEntity.setChanged();
            }
            spawnAmbientEndRod(serverLevel, pos);
            return;
        }

        blockEntity.exposureTicks++;
        blockEntity.setChanged();
        if (blockEntity.exposureTicks % AMBIENT_SMOKE_INTERVAL == 0) {
            spawnAmbientSmoke(serverLevel, pos);
        }

        if (blockEntity.exposureTicks >= CLEAR_DAYLIGHT_DURATION_TICKS) {
            wither(serverLevel, pos, state);
        }
    }

    private static boolean isInWitheringEnvironment(ServerLevel level, BlockPos pos) {
        if (level.dimension().equals(Level.NETHER)) {
            return true;
        }

        BlockPos skyCheckPos = pos.above();
        return level.isDay()
                && level.canSeeSky(skyCheckPos)
                && !level.isRainingAt(skyCheckPos)
                && !level.isThundering();
    }

    private static void spawnAmbientSmoke(ServerLevel level, BlockPos pos) {
        double x = pos.getX() + 0.35D + level.random.nextDouble() * 0.3D;
        double y = pos.getY() + 0.55D + level.random.nextDouble() * 0.25D;
        double z = pos.getZ() + 0.35D + level.random.nextDouble() * 0.3D;
        level.sendParticles(ParticleTypes.SMOKE, x, y, z, 1, 0.01D, 0.02D, 0.01D, 0.005D);
    }

    private static void spawnAmbientEndRod(ServerLevel level, BlockPos pos) {
        if (level.random.nextInt(AMBIENT_END_ROD_AVERAGE_INTERVAL) != 0) {
            return;
        }

        double x = pos.getX() + 0.35D + level.random.nextDouble() * 0.3D;
        double y = pos.getY() + 0.45D + level.random.nextDouble() * 0.35D;
        double z = pos.getZ() + 0.35D + level.random.nextDouble() * 0.3D;
        level.sendParticles(ParticleTypes.END_ROD, x, y, z, 1, 0.01D, 0.02D, 0.01D, 0.005D);
    }

    private static void applyAbsorptionAura(ServerLevel level, BlockPos pos) {
        double x = pos.getX() + 0.5D;
        double y = pos.getY() + 0.5D;
        double z = pos.getZ() + 0.5D;
        AABB bounds = new AABB(
                x - ABSORPTION_AURA_RANGE, y - ABSORPTION_AURA_RANGE, z - ABSORPTION_AURA_RANGE,
                x + ABSORPTION_AURA_RANGE, y + ABSORPTION_AURA_RANGE, z + ABSORPTION_AURA_RANGE);

        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, bounds,
                candidate -> candidate.isAlive() && candidate.distanceToSqr(x, y, z) <= ABSORPTION_AURA_RANGE_SQUARED)) {
            EphemeralBloomAbsorption.grant(entity);
        }
    }

    private static void wither(ServerLevel level, BlockPos pos, BlockState state) {
        level.sendParticles(ParticleTypes.LARGE_SMOKE,
                pos.getX() + 0.5D, pos.getY() + 0.65D, pos.getZ() + 0.5D,
                8, 0.2D, 0.2D, 0.2D, 0.02D);
        level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS,
                1.0F, 2.6F + (level.random.nextFloat() - level.random.nextFloat()) * 0.8F);

        BlockState witheredState = state.is(ModBlocks.POTTED_EPHEMERAL_BLOOM.get())
                ? Blocks.POTTED_DEAD_BUSH.defaultBlockState()
                : Blocks.DEAD_BUSH.defaultBlockState();
        level.setBlock(pos, witheredState, Block.UPDATE_ALL);
    }
}

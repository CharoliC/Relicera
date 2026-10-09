package com.yukari.relicera.common.block;

import com.yukari.relicera.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class PottedEphemeralBloomBlock extends FlowerPotBlock implements EntityBlock {
    public PottedEphemeralBloomBlock(Supplier<FlowerPotBlock> emptyPot, Supplier<? extends Block> flower, BlockBehaviour.Properties properties) {
        super(emptyPot, flower, properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new EphemeralBloomBlockEntity(pos, state);
    }

    @Nullable
    @Override
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        if (level.isClientSide || blockEntityType != ModBlockEntities.EPHEMERAL_BLOOM.get()) {
            return null;
        }
        return (tickerLevel, pos, tickerState, blockEntity) ->
                EphemeralBloomBlockEntity.tick(tickerLevel, pos, tickerState, (EphemeralBloomBlockEntity) blockEntity);
    }
}

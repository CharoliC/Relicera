package com.yukari.relicera.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

public class RelicRepairTableBlock extends Block implements EntityBlock {
    private static final VoxelShape COLLISION_SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 12.0D, 16.0D);
    private static final VoxelShape OUTLINE_SHAPE = Shapes.or(
            Block.box(0.0D, 0.0D, 0.0D, 16.0D, 3.0D, 16.0D),
            Block.box(0.0D, 9.0D, 0.0D, 16.0D, 11.0D, 16.0D),
            Block.box(4.0D, 11.0D, 12.0D, 16.0D, 12.0D, 16.0D),
            Block.box(0.0D, 11.0D, 4.0D, 4.0D, 12.0D, 16.0D),
            Block.box(0.0D, 11.0D, 0.0D, 12.0D, 12.0D, 4.0D),
            Block.box(12.0D, 11.0D, 0.0D, 16.0D, 12.0D, 12.0D),
            Block.box(12.0D, 3.0D, 1.0D, 14.0D, 9.0D, 2.0D),
            Block.box(1.0D, 3.0D, 1.0D, 4.0D, 9.0D, 2.0D),
            Block.box(4.0D, 3.0D, 1.0D, 12.0D, 4.0D, 2.0D),
            Block.box(4.0D, 8.0D, 1.0D, 12.0D, 9.0D, 2.0D),
            Block.box(9.0D, 4.0D, 1.0D, 10.0D, 8.0D, 2.0D),
            Block.box(6.0D, 4.0D, 1.0D, 7.0D, 8.0D, 2.0D),
            Block.box(2.0D, 3.0D, 2.0D, 14.0D, 9.0D, 14.0D),
            Block.box(1.0D, 3.0D, 2.0D, 2.0D, 9.0D, 4.0D),
            Block.box(1.0D, 3.0D, 12.0D, 2.0D, 9.0D, 15.0D),
            Block.box(1.0D, 3.0D, 4.0D, 2.0D, 4.0D, 12.0D),
            Block.box(1.0D, 8.0D, 4.0D, 2.0D, 9.0D, 12.0D),
            Block.box(1.0D, 4.0D, 6.0D, 2.0D, 8.0D, 7.0D),
            Block.box(1.0D, 4.0D, 9.0D, 2.0D, 8.0D, 10.0D),
            Block.box(2.0D, 3.0D, 14.0D, 4.0D, 9.0D, 15.0D),
            Block.box(12.0D, 3.0D, 14.0D, 15.0D, 9.0D, 15.0D),
            Block.box(4.0D, 3.0D, 14.0D, 12.0D, 4.0D, 15.0D),
            Block.box(4.0D, 8.0D, 14.0D, 12.0D, 9.0D, 15.0D),
            Block.box(6.0D, 4.0D, 14.0D, 7.0D, 8.0D, 15.0D),
            Block.box(9.0D, 4.0D, 14.0D, 10.0D, 8.0D, 15.0D),
            Block.box(14.0D, 3.0D, 12.0D, 15.0D, 9.0D, 14.0D),
            Block.box(14.0D, 3.0D, 1.0D, 15.0D, 9.0D, 4.0D),
            Block.box(14.0D, 3.0D, 4.0D, 15.0D, 4.0D, 12.0D),
            Block.box(14.0D, 8.0D, 4.0D, 15.0D, 9.0D, 12.0D),
            Block.box(14.0D, 4.0D, 9.0D, 15.0D, 8.0D, 10.0D),
            Block.box(14.0D, 4.0D, 6.0D, 15.0D, 8.0D, 7.0D)
    );

    public RelicRepairTableBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return OUTLINE_SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return COLLISION_SHAPE;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RelicRepairTableBlockEntity(pos, state);
    }

    @Nullable
    @Override
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return (tickerLevel, pos, tickerState, blockEntity) -> {
            if (blockEntity instanceof RelicRepairTableBlockEntity relicRepairTable) {
                RelicRepairTableBlockEntity.tick(tickerLevel, pos, tickerState, relicRepairTable);
            }
        };
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof RelicRepairTableBlockEntity relicRepairTable && player instanceof ServerPlayer serverPlayer) {
            NetworkHooks.openScreen(serverPlayer, relicRepairTable, pos);
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof RelicRepairTableBlockEntity relicRepairTable) {
                relicRepairTable.dropContents();
                level.updateNeighbourForOutputSignal(pos, this);
            }
        }

        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}

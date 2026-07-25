package net.evarius.tnadditions.block.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;

/**
 * Directional roadside delineator with four model-accurate shapes. The supplied
 * model is intentionally offset toward its facing side, so merely rotating one
 * centered shape would separate the selection box from the rendered geometry.
 */
public class LeitpfostenBlock extends HorizontalFacingBlock {
    public static final MapCodec<LeitpfostenBlock> CODEC = createCodec(LeitpfostenBlock::new);

    private static final VoxelShape NORTH_SHAPE =
            Block.createCuboidShape(7.74, 0, 4, 9.265, 20.025, 8);
    private static final VoxelShape EAST_SHAPE =
            Block.createCuboidShape(8, 0, 7.74, 12, 20.025, 9.265);
    private static final VoxelShape SOUTH_SHAPE =
            Block.createCuboidShape(6.735, 0, 8, 8.26, 20.025, 12);
    private static final VoxelShape WEST_SHAPE =
            Block.createCuboidShape(4, 0, 6.735, 8, 20.025, 8.26);

    public LeitpfostenBlock(Settings settings) {
        super(settings);
        setDefaultState(getStateManager().getDefaultState().with(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends HorizontalFacingBlock> getCodec() {
        return CODEC;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        return getDefaultState().with(FACING, context.getHorizontalPlayerFacing().getOpposite());
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return switch (state.get(FACING)) {
            case EAST -> EAST_SHAPE;
            case SOUTH -> SOUTH_SHAPE;
            case WEST -> WEST_SHAPE;
            default -> NORTH_SHAPE;
        };
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return getOutlineShape(state, world, pos, context);
    }
}

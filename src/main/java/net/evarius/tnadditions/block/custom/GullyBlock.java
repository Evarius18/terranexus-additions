package net.evarius.tnadditions.block.custom;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockSetType;
import net.minecraft.block.BlockState;
import net.minecraft.block.TrapdoorBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.enums.BlockHalf;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;

public final class GullyBlock extends TrapdoorBlock {
    private static final VoxelShape CLOSED_BOTTOM_SHAPE = createCuboidShape(0, 0, 0, 16, 2.125, 16);
    private static final VoxelShape CLOSED_TOP_SHAPE = createCuboidShape(0, 13.875, 0, 16, 16, 16);
    private static final VoxelShape OPEN_NORTH_SHAPE = createCuboidShape(0, 0, 13.875, 16, 16, 16);
    private static final VoxelShape OPEN_SOUTH_SHAPE = createCuboidShape(0, 0, 0, 16, 16, 2.125);
    private static final VoxelShape OPEN_WEST_SHAPE = createCuboidShape(13.875, 0, 0, 16, 16, 16);
    private static final VoxelShape OPEN_EAST_SHAPE = createCuboidShape(0, 0, 0, 2.125, 16, 16);

    public GullyBlock(AbstractBlock.Settings settings) {
        super(BlockSetType.COPPER, settings);
    }

    @Override
    protected VoxelShape getOutlineShape(
            BlockState state,
            BlockView world,
            BlockPos pos,
            ShapeContext context
    ) {
        if (!state.get(OPEN)) {
            return state.get(HALF) == BlockHalf.TOP
                    ? CLOSED_TOP_SHAPE
                    : CLOSED_BOTTOM_SHAPE;
        }
        return switch (state.get(FACING)) {
            case NORTH -> OPEN_NORTH_SHAPE;
            case SOUTH -> OPEN_SOUTH_SHAPE;
            case WEST -> OPEN_WEST_SHAPE;
            default -> OPEN_EAST_SHAPE;
        };
    }

}

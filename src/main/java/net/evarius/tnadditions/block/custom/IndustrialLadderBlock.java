package net.evarius.tnadditions.block.custom;

import net.minecraft.block.BlockState;
import net.minecraft.block.LadderBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;

import java.util.Map;

/** Climbable industrial ladder retaining the supplied three-dimensional rungs. */
public final class IndustrialLadderBlock extends LadderBlock {
    private static final Map<Direction, VoxelShape> SHAPES = Map.of(
            Direction.NORTH, createCuboidShape(3, 3, 11, 13, 14, 16),
            Direction.SOUTH, createCuboidShape(3, 3, 0, 13, 14, 5),
            Direction.EAST, createCuboidShape(0, 3, 3, 5, 14, 13),
            Direction.WEST, createCuboidShape(11, 3, 3, 16, 14, 13));

    public IndustrialLadderBlock(Settings settings) { super(settings); }
    @Override protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPES.get(state.get(FACING));
    }
}

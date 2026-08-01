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

import java.util.EnumMap;
import java.util.Map;

/** Lightweight rotatable base for non-full decorative geometry. */
public final class DirectionalDecorationBlock extends HorizontalFacingBlock {
    private final Map<Direction, VoxelShape> shapes = new EnumMap<>(Direction.class);

    public DirectionalDecorationBlock(Settings settings, double minX, double minY, double minZ,
                                      double maxX, double maxY, double maxZ) {
        super(settings);
        setDefaultState(getStateManager().getDefaultState().with(FACING, Direction.NORTH));
        shapes.put(Direction.NORTH, Block.createCuboidShape(minX, minY, minZ, maxX, maxY, maxZ));
        shapes.put(Direction.EAST, Block.createCuboidShape(16 - maxZ, minY, minX, 16 - minZ, maxY, maxX));
        shapes.put(Direction.SOUTH, Block.createCuboidShape(16 - maxX, minY, 16 - maxZ, 16 - minX, maxY, 16 - minZ));
        shapes.put(Direction.WEST, Block.createCuboidShape(minZ, minY, 16 - maxX, maxZ, maxY, 16 - minX));
    }

    @Override protected MapCodec<? extends HorizontalFacingBlock> getCodec() { return MapCodec.unit(this); }
    @Override protected void appendProperties(StateManager.Builder<Block, BlockState> builder) { builder.add(FACING); }
    @Override public BlockState getPlacementState(ItemPlacementContext context) {
        return getDefaultState().with(FACING, context.getHorizontalPlayerFacing().getOpposite());
    }
    @Override protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return shapes.get(state.get(FACING));
    }
}

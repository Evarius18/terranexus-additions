package net.evarius.tnadditions.block.custom;

import net.evarius.tnadditions.config.GuardrailOxidationConfig;
import net.minecraft.block.BlockState;
import net.minecraft.block.Block;
import net.minecraft.block.Oxidizable;
import net.minecraft.block.ShapeContext;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;

/**
 * Oxidizable variant of the simple guardrail. Other guardrail shapes deliberately
 * keep using {@link GuardrailBlock} until their own oxidation chains are added.
 */
public class OxidizingGuardrailBlock extends GuardrailBlock implements Oxidizable {
    private static final VoxelShape ALONG_Z_SHAPE = VoxelShapes.union(
            Block.createCuboidShape(7, 6, 0, 11, 16, 16),
            Block.createCuboidShape(7, 0, 3, 9, 15, 5),
            Block.createCuboidShape(7, 0, 11, 9, 15, 13)
    );
    private static final VoxelShape ALONG_X_SHAPE = VoxelShapes.union(
            Block.createCuboidShape(0, 6, 5, 16, 16, 9),
            Block.createCuboidShape(3, 0, 7, 5, 15, 9),
            Block.createCuboidShape(11, 0, 7, 13, 15, 9)
    );

    private final OxidationLevel oxidationLevel;

    public OxidizingGuardrailBlock(OxidationLevel oxidationLevel, Settings settings) {
        super(settings);
        this.oxidationLevel = oxidationLevel;
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        return getDefaultState().with(FACING, context.getHorizontalPlayerFacing());
    }

    @Override
    protected void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (GuardrailOxidationConfig.isEnabled()) {
            tickDegradation(state, world, pos, random);
        }
    }

    @Override
    protected boolean hasRandomTicks(BlockState state) {
        return GuardrailOxidationConfig.isEnabled()
                && Oxidizable.getIncreasedOxidationBlock(state.getBlock()).isPresent();
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return state.get(FACING).getAxis() == Direction.Axis.Z ? ALONG_X_SHAPE : ALONG_Z_SHAPE;
    }

    @Override
    public OxidationLevel getDegradationLevel() {
        return oxidationLevel;
    }
}

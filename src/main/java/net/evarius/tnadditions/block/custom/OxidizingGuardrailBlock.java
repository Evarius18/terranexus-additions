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
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.block.WireOrientation;

/**
 * Shared oxidation and support behavior for the simple guardrail family.
 */
public class OxidizingGuardrailBlock extends GuardrailBlock implements Oxidizable {
    private static final int SUPPORT_SCAN_DISTANCE = 4;
    private static final VoxelShape ALONG_Z_RAIL = Block.createCuboidShape(7, 6, 0, 11, 16, 16);
    private static final VoxelShape ALONG_X_RAIL = Block.createCuboidShape(0, 6, 5, 16, 16, 9);
    private static final VoxelShape ALONG_Z_STANDARD = VoxelShapes.union(
            ALONG_Z_RAIL,
            Block.createCuboidShape(7, 0, 3, 9, 15, 5),
            Block.createCuboidShape(7, 0, 11, 9, 15, 13)
    );
    private static final VoxelShape ALONG_X_STANDARD = VoxelShapes.union(
            ALONG_X_RAIL,
            Block.createCuboidShape(3, 0, 7, 5, 15, 9),
            Block.createCuboidShape(11, 0, 7, 13, 15, 9)
    );
    private static final VoxelShape ALONG_Z_CENTER_POST = VoxelShapes.union(
            ALONG_Z_RAIL,
            Block.createCuboidShape(7, 0, 7, 9, 15, 9)
    );
    private static final VoxelShape ALONG_X_CENTER_POST = VoxelShapes.union(
            ALONG_X_RAIL,
            Block.createCuboidShape(7, 0, 7, 9, 15, 9)
    );

    private final OxidationLevel oxidationLevel;
    private final GuardrailVariant variant;

    public OxidizingGuardrailBlock(OxidationLevel oxidationLevel, GuardrailVariant variant, Settings settings) {
        super(settings);
        this.oxidationLevel = oxidationLevel;
        this.variant = variant;
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        BlockState state = getDefaultState().with(FACING, context.getHorizontalPlayerFacing());
        return variant == GuardrailVariant.WITHOUT_POSTS
                && !hasValidSupportChain(state, context.getWorld(), context.getBlockPos()) ? null : state;
    }

    @Override
    protected boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        return variant != GuardrailVariant.WITHOUT_POSTS || hasValidSupportChain(state, world, pos);
    }

    @Override
    protected void neighborUpdate(
            BlockState state,
            World world,
            BlockPos pos,
            Block sourceBlock,
            WireOrientation wireOrientation,
            boolean notify
    ) {
        if (!world.isClient() && variant == GuardrailVariant.WITHOUT_POSTS) {
            world.scheduleBlockTick(pos, this, 1);
        }
    }

    @Override
    protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (variant == GuardrailVariant.WITHOUT_POSTS && !hasValidSupportChain(state, world, pos)) {
            world.breakBlock(pos, true);
        }
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
        boolean alongX = railDirection(state).getAxis() == Direction.Axis.X;
        return switch (variant) {
            case WITHOUT_POSTS -> alongX ? ALONG_X_RAIL : ALONG_Z_RAIL;
            case CENTER_POST -> alongX ? ALONG_X_CENTER_POST : ALONG_Z_CENTER_POST;
            default -> alongX ? ALONG_X_STANDARD : ALONG_Z_STANDARD;
        };
    }

    @Override
    public OxidationLevel getDegradationLevel() {
        return oxidationLevel;
    }

    public GuardrailVariant getVariant() {
        return variant;
    }

    private boolean hasValidSupportChain(BlockState state, WorldView world, BlockPos pos) {
        Direction direction = railDirection(state);
        ChainEnd positive = scanChainEnd(world, pos, direction, state);
        ChainEnd negative = scanChainEnd(world, pos, direction.getOpposite(), state);
        int postlessCount = 1 + positive.postlessBlocks() + negative.postlessBlocks();

        if (positive.support() == null && negative.support() == null) {
            return false;
        }
        if (positive.support() == null || negative.support() == null) {
            return postlessCount == 1;
        }

        int maximumSpan = Math.min(
                positive.support().getMaximumPostlessSpan(),
                negative.support().getMaximumPostlessSpan()
        );
        return postlessCount <= maximumSpan;
    }

    private static ChainEnd scanChainEnd(
            WorldView world,
            BlockPos origin,
            Direction direction,
            BlockState reference
    ) {
        int postlessBlocks = 0;
        for (int distance = 1; distance <= SUPPORT_SCAN_DISTANCE; distance++) {
            BlockState candidate = world.getBlockState(origin.offset(direction, distance));
            if (!(candidate.getBlock() instanceof OxidizingGuardrailBlock guardrail)
                    || railDirection(candidate).getAxis() != railDirection(reference).getAxis()) {
                return new ChainEnd(postlessBlocks, null);
            }
            if (guardrail.variant == GuardrailVariant.WITHOUT_POSTS) {
                postlessBlocks++;
                continue;
            }
            return guardrail.variant.isSupporting()
                    ? new ChainEnd(postlessBlocks, guardrail.variant)
                    : new ChainEnd(postlessBlocks, null);
        }
        return new ChainEnd(postlessBlocks, null);
    }

    private static Direction railDirection(BlockState state) {
        return state.get(FACING).rotateYClockwise();
    }

    private record ChainEnd(int postlessBlocks, GuardrailVariant support) {
    }
}

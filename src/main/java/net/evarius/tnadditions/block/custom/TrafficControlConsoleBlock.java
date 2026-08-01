package net.evarius.tnadditions.block.custom;

import com.mojang.serialization.MapCodec;
import net.evarius.tnadditions.infrastructure.InfrastructureAccess;
import net.evarius.tnadditions.traffic.TrafficControlNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

/** Physical entry point for the server-authoritative traffic control UI. */
public final class TrafficControlConsoleBlock extends HorizontalFacingBlock {
    private final boolean wallScreen;
    public TrafficControlConsoleBlock(Settings settings) { this(settings, false); }
    public TrafficControlConsoleBlock(Settings settings, boolean wallScreen) {
        super(settings);
        this.wallScreen = wallScreen;
        setDefaultState(getStateManager().getDefaultState().with(FACING, Direction.NORTH));
    }
    @Override protected MapCodec<? extends HorizontalFacingBlock> getCodec() { return MapCodec.unit(this); }
    @Override protected void appendProperties(StateManager.Builder<Block, BlockState> builder) { builder.add(FACING); }
    @Override public BlockState getPlacementState(ItemPlacementContext context) {
        return getDefaultState().with(FACING, context.getHorizontalPlayerFacing().getOpposite());
    }
    @Override protected ActionResult onUse(BlockState state, World world, BlockPos pos,
                                           PlayerEntity user, BlockHitResult hit) {
        if (world.isClient()) return ActionResult.SUCCESS;
        if (!(world instanceof ServerWorld serverWorld) || !(user instanceof ServerPlayerEntity player))
            return ActionResult.PASS;
        if (!InfrastructureAccess.mayConfigure(player) || !InfrastructureAccess.mayInteract(player, serverWorld, pos))
            return ActionResult.FAIL;
        TrafficControlNetworking.open(player);
        return ActionResult.SUCCESS_SERVER;
    }
    @Override protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        if (!wallScreen) return createCuboidShape(1, 0, 1, 15, 10, 15);
        return switch (state.get(FACING)) {
            case NORTH -> createCuboidShape(1, 2, 13, 15, 14, 16);
            case SOUTH -> createCuboidShape(1, 2, 0, 15, 14, 3);
            case EAST -> createCuboidShape(0, 2, 1, 3, 14, 15);
            default -> createCuboidShape(13, 2, 1, 16, 14, 15);
        };
    }
}

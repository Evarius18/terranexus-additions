package net.evarius.tnadditions.block.custom;

import com.mojang.serialization.MapCodec;
import net.evarius.tnadditions.garage.GarageDoorOpeningType;
import net.evarius.tnadditions.garage.GarageAccessState;
import net.evarius.tnadditions.config.InfrastructureConfig;
import net.evarius.tnadditions.infrastructure.InfrastructureAccess;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.block.WireOrientation;

import java.util.ArrayList;
import java.util.List;

/** Animated, redstone-ready garage-door segment. Connected vertical segments move together. */
public final class AnimatedGarageDoorBlock extends HorizontalFacingBlock {
    public static final MapCodec<AnimatedGarageDoorBlock> CODEC = createCodec(AnimatedGarageDoorBlock::new);
    public static final IntProperty OPEN_PROGRESS = IntProperty.of("open_progress", 0, 4);
    public static final BooleanProperty OPEN = BooleanProperty.of("open");
    public static final BooleanProperty POWERED = BooleanProperty.of("powered");
    private final GarageDoorOpeningType openingType;

    public AnimatedGarageDoorBlock(Settings settings) { this(settings, GarageDoorOpeningType.SECTIONAL); }
    public AnimatedGarageDoorBlock(Settings settings, GarageDoorOpeningType openingType) {
        super(settings);
        this.openingType = openingType;
        setDefaultState(getStateManager().getDefaultState().with(FACING, Direction.NORTH)
                .with(OPEN_PROGRESS, 0).with(OPEN, false).with(POWERED, false));
    }

    public GarageDoorOpeningType openingType() { return openingType; }
    @Override protected MapCodec<? extends HorizontalFacingBlock> getCodec() { return CODEC; }
    @Override protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, OPEN_PROGRESS, OPEN, POWERED);
    }
    @Override public BlockState getPlacementState(ItemPlacementContext context) {
        boolean powered = context.getWorld().isReceivingRedstonePower(context.getBlockPos());
        return getDefaultState().with(FACING, context.getHorizontalPlayerFacing().getOpposite())
                .with(POWERED, powered).with(OPEN, powered).with(OPEN_PROGRESS, powered ? 4 : 0);
    }

    @Override public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
        super.onPlaced(world, pos, state, placer, itemStack);
        if (world instanceof ServerWorld serverWorld && placer instanceof ServerPlayerEntity player) {
            GarageAccessState.get(serverWorld.getServer()).register(serverWorld, pos, player.getUuid());
        }
    }

    @Override protected ActionResult onUse(BlockState state, World world, BlockPos pos,
                                           PlayerEntity user, BlockHitResult hit) {
        if (world.isClient()) return ActionResult.SUCCESS;
        if (!(world instanceof ServerWorld serverWorld) || !(user instanceof ServerPlayerEntity player))
            return ActionResult.PASS;
        List<BlockPos> column = column(serverWorld, pos, state.get(FACING));
        if (!GarageAccessState.get(serverWorld.getServer()).permits(player, serverWorld, pos)) {
            player.sendMessage(Text.translatable("message.terranexus.garage.no_permission").formatted(Formatting.RED), true);
            return ActionResult.FAIL;
        }
        for (BlockPos part : column) if (!InfrastructureAccess.mayInteract(player, serverWorld, part)) {
            player.sendMessage(Text.literal("Keine Berechtigung für dieses Garagentor.").formatted(Formatting.RED), true);
            return ActionResult.FAIL;
        }
        setTarget(serverWorld, column, !state.get(OPEN), state.get(FACING), state.get(POWERED));
        return ActionResult.SUCCESS_SERVER;
    }

    public ActionResult toggleFromController(ServerWorld world, BlockPos pos, ServerPlayerEntity player) {
        BlockState state = world.getBlockState(pos);
        if (!state.isOf(this)) return ActionResult.PASS;
        List<BlockPos> parts = column(world, pos, state.get(FACING));
        if (!GarageAccessState.get(world.getServer()).permits(player, world, pos)) {
            player.sendMessage(Text.translatable("message.terranexus.garage.no_permission").formatted(Formatting.RED), true);
            return ActionResult.FAIL;
        }
        for (BlockPos part : parts) {
            if (!InfrastructureAccess.mayInteract(player, world, part)) {
                player.sendMessage(Text.translatable("message.terranexus.garage.no_permission").formatted(Formatting.RED), true);
                return ActionResult.FAIL;
            }
        }
        setTarget(world, parts, !state.get(OPEN), state.get(FACING), state.get(POWERED));
        return ActionResult.SUCCESS_SERVER;
    }

    @Override protected void onStateReplaced(BlockState state, ServerWorld world, BlockPos pos, boolean moved) {
        GarageAccessState.get(world.getServer()).remove(world, pos);
        super.onStateReplaced(state, world, pos, moved);
    }

    @Override protected void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock,
                                            WireOrientation wireOrientation, boolean notify) {
        if (world.isClient()) return;
        boolean powered = world.isReceivingRedstonePower(pos);
        if (powered == state.get(POWERED)) return;
        List<BlockPos> column = column(world, pos, state.get(FACING));
        setTarget(world, column, powered, state.get(FACING), powered);
    }

    @Override protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        int current = state.get(OPEN_PROGRESS);
        int target = state.get(OPEN) ? 4 : 0;
        if (current == target) return;
        int next = current + Integer.signum(target - current);
        world.setBlockState(pos, state.with(OPEN_PROGRESS, next), Block.NOTIFY_LISTENERS);
        if (next != target) world.scheduleBlockTick(pos, this, InfrastructureConfig.garageAnimationTicks());
    }

    @Override protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        int progress = state.get(OPEN_PROGRESS);
        if (progress >= 4) return Block.createCuboidShape(0, 13, 0, 16, 16, 16);
        double minY = progress * 4.0;
        return switch (state.get(FACING)) {
            case NORTH -> Block.createCuboidShape(0, minY, 13, 16, 16, 16);
            case SOUTH -> Block.createCuboidShape(0, minY, 0, 16, 16, 3);
            case EAST -> Block.createCuboidShape(0, minY, 0, 3, 16, 16);
            case WEST -> Block.createCuboidShape(13, minY, 0, 16, 16, 16);
            default -> VoxelShapes.fullCube();
        };
    }

    private void setTarget(World world, List<BlockPos> column, boolean open, Direction facing, boolean powered) {
        for (BlockPos part : column) {
            BlockState partState = world.getBlockState(part);
            if (!partState.isOf(this) || partState.get(FACING) != facing) continue;
            world.setBlockState(part, partState.with(OPEN, open).with(POWERED, powered), Block.NOTIFY_LISTENERS);
            world.scheduleBlockTick(part, this, InfrastructureConfig.garageAnimationTicks());
        }
    }

    private List<BlockPos> column(World world, BlockPos origin, Direction facing) {
        List<BlockPos> positions = new ArrayList<>();
        BlockPos start = origin;
        for (int offset = 1; offset <= 15; offset++) {
            BlockPos candidate = origin.down(offset);
            BlockState state = world.getBlockState(candidate);
            if (!state.isOf(this) || state.get(FACING) != facing) break;
            start = candidate;
        }
        for (int offset = 0; offset < 32; offset++) {
            BlockPos candidate = start.up(offset);
            BlockState state = world.getBlockState(candidate);
            if (!state.isOf(this) || state.get(FACING) != facing) break;
            positions.add(candidate.toImmutable());
        }
        return positions;
    }
}

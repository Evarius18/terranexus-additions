package net.evarius.tnadditions.block.custom;

import com.mojang.serialization.MapCodec;
import net.evarius.tnadditions.config.InfrastructureConfig;
import net.evarius.tnadditions.infrastructure.InfrastructureAccess;
import net.evarius.tnadditions.traffic.*;
import net.minecraft.block.*;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public final class TrafficLightBlock extends HorizontalFacingBlock {
    public static final MapCodec<TrafficLightBlock> CODEC = createCodec(TrafficLightBlock::new);
    public static final EnumProperty<TrafficSignalAspect> ASPECT = EnumProperty.of("aspect", TrafficSignalAspect.class);
    public TrafficLightBlock(Settings settings) { super(settings); setDefaultState(getStateManager().getDefaultState().with(FACING, Direction.NORTH).with(ASPECT, TrafficSignalAspect.RED)); }
    @Override protected MapCodec<? extends HorizontalFacingBlock> getCodec() { return CODEC; }
    @Override protected void appendProperties(StateManager.Builder<Block, BlockState> builder) { builder.add(FACING, ASPECT); }
    @Override public BlockState getPlacementState(ItemPlacementContext context) { return getDefaultState().with(FACING, context.getHorizontalPlayerFacing().getOpposite()); }
    @Override public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.onPlaced(world, pos, state, placer, stack);
        if (world instanceof ServerWorld sw) { TrafficControlState.get(sw.getServer()).register(sw, pos, TrafficDeviceType.SIGNAL); sw.scheduleBlockTick(pos, this, 1); }
    }
    @Override protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        TrafficControlState control = TrafficControlState.get(world.getServer());
        TrafficDevice device = control.register(world, pos, TrafficDeviceType.SIGNAL);
        TrafficSignalAspect aspect = device == null ? TrafficSignalAspect.OFF : control.aspect(device, world.getTime());
        if (state.get(ASPECT) != aspect) world.setBlockState(pos, state.with(ASPECT, aspect), Block.NOTIFY_LISTENERS);
        world.scheduleBlockTick(pos, this, InfrastructureConfig.signalUpdateTicks());
    }
    @Override protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity user, BlockHitResult hit) {
        if (world.isClient()) return ActionResult.SUCCESS;
        if (!(world instanceof ServerWorld sw) || !(user instanceof ServerPlayerEntity player)) return ActionResult.PASS;
        if (!InfrastructureAccess.mayConfigure(player) || !InfrastructureAccess.mayInteract(player, sw, pos)) return ActionResult.FAIL;
        TrafficDevice device = TrafficControlState.get(sw.getServer()).register(sw, pos, TrafficDeviceType.SIGNAL);
        if (device != null) player.sendMessage(net.minecraft.text.Text.literal("Traffic light: " + device.groupId() + " / " + device.programId()), true);
        return ActionResult.SUCCESS_SERVER;
    }
    @Override protected void onStateReplaced(BlockState state, ServerWorld world, BlockPos pos, boolean moved) {
        if (!moved) TrafficControlState.get(world.getServer()).remove(world, pos);
        super.onStateReplaced(state, world, pos, moved);
    }
    @Override protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return switch(state.get(FACING)) { case NORTH -> createCuboidShape(4,0,12,12,16,16); case SOUTH -> createCuboidShape(4,0,0,12,16,4); case EAST -> createCuboidShape(0,0,4,4,16,12); default -> createCuboidShape(12,0,4,16,16,12); };
    }
}

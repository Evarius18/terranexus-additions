package net.evarius.tnadditions.block.custom;

import com.mojang.serialization.MapCodec;
import net.evarius.tnadditions.infrastructure.InfrastructureAccess;
import net.evarius.tnadditions.config.InfrastructureConfig;
import net.evarius.tnadditions.traffic.TrafficControlState;
import net.evarius.tnadditions.traffic.TrafficDeviceType;
import net.evarius.tnadditions.traffic.TrafficDisplayMode;
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

public final class DigitalTrafficDisplayBlock extends HorizontalFacingBlock {
    public static final MapCodec<DigitalTrafficDisplayBlock> CODEC = createCodec(DigitalTrafficDisplayBlock::new);
    public static final EnumProperty<TrafficDisplayMode> MODE = EnumProperty.of("mode", TrafficDisplayMode.class);
    public DigitalTrafficDisplayBlock(Settings settings) {
        super(settings); setDefaultState(getStateManager().getDefaultState().with(FACING, Direction.NORTH).with(MODE, TrafficDisplayMode.OFF));
    }
    @Override protected MapCodec<? extends HorizontalFacingBlock> getCodec() { return CODEC; }
    @Override protected void appendProperties(StateManager.Builder<Block, BlockState> builder) { builder.add(FACING, MODE); }
    @Override public BlockState getPlacementState(ItemPlacementContext context) { return getDefaultState().with(FACING, context.getHorizontalPlayerFacing().getOpposite()); }
    @Override public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.onPlaced(world, pos, state, placer, stack);
        if (world instanceof ServerWorld serverWorld) {
            TrafficControlState.get(serverWorld.getServer()).register(serverWorld, pos, TrafficDeviceType.DISPLAY);
            serverWorld.scheduleBlockTick(pos, this, 1);
        }
    }
    @Override protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        var device = TrafficControlState.get(world.getServer()).register(world, pos, TrafficDeviceType.DISPLAY);
        TrafficDisplayMode mode = device != null && device.enabled() && device.scheduledActive(world.getTime())
                ? TrafficDisplayMode.parse(device.displayMode()) : TrafficDisplayMode.OFF;
        if (state.get(MODE) != mode) world.setBlockState(pos, state.with(MODE, mode), Block.NOTIFY_LISTENERS);
        world.scheduleBlockTick(pos, this, InfrastructureConfig.signalUpdateTicks());
    }
    @Override protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity user, BlockHitResult hit) {
        if (world.isClient()) return ActionResult.SUCCESS;
        if (!(world instanceof ServerWorld sw) || !(user instanceof ServerPlayerEntity player)) return ActionResult.PASS;
        if (!InfrastructureAccess.mayConfigure(player) || !InfrastructureAccess.mayInteract(player, sw, pos)) return ActionResult.FAIL;
        var device = TrafficControlState.get(sw.getServer()).register(sw, pos, TrafficDeviceType.DISPLAY);
        if (device != null) player.sendMessage(net.minecraft.text.Text.literal("Traffic display: " + device.groupId() + " / " + device.displayMode()), true);
        return ActionResult.SUCCESS_SERVER;
    }
    @Override protected void onStateReplaced(BlockState state, ServerWorld world, BlockPos pos, boolean moved) {
        if (!moved) TrafficControlState.get(world.getServer()).remove(world, pos);
        super.onStateReplaced(state, world, pos, moved);
    }
    @Override protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return switch(state.get(FACING)) { case NORTH -> createCuboidShape(1,2,13,15,14,16); case SOUTH -> createCuboidShape(1,2,0,15,14,3); case EAST -> createCuboidShape(0,2,1,3,14,15); default -> createCuboidShape(13,2,1,16,14,15); };
    }
}

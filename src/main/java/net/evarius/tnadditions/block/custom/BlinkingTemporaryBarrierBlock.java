package net.evarius.tnadditions.block.custom;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

/**
 * A temporary barrier whose visible lamp and emitted block light share one
 * server-controlled state. A short flash followed by a longer pause resembles
 * common construction warning lights more closely than a looping texture.
 */
public class BlinkingTemporaryBarrierBlock extends TemporaryBarrierBlock {
    public static final BooleanProperty LIT = Properties.LIT;
    public static final MapCodec<BlinkingTemporaryBarrierBlock> CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Profile.CODEC.fieldOf("profile").forGetter(block -> block.profile),
                    createSettingsCodec()
            ).apply(instance, BlinkingTemporaryBarrierBlock::new));

    private static final int FLASH_ON_TICKS = 6;
    private static final int FLASH_OFF_TICKS = 14;

    public BlinkingTemporaryBarrierBlock(Profile profile, Settings settings) {
        super(profile, settings);
        setDefaultState(getDefaultState().with(LIT, false));
    }

    @Override
    protected MapCodec<? extends BlinkingTemporaryBarrierBlock> getCodec() {
        return CODEC;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(LIT);
    }

    @Override
    protected void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        if (!world.isClient() && !oldState.isOf(state.getBlock())) {
            world.scheduleBlockTick(pos, this, FLASH_OFF_TICKS);
        }
    }

    @Override
    protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        boolean lit = !state.get(LIT);
        world.setBlockState(pos, state.with(LIT, lit), Block.NOTIFY_ALL);
        world.scheduleBlockTick(pos, this, lit ? FLASH_ON_TICKS : FLASH_OFF_TICKS);
    }
}

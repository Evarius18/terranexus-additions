package net.evarius.tnadditions.block.custom;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;

/**
 * A directly placeable road surface with one immutable pixel height.
 * Different registry entries share this implementation and their material.
 */
public final class FixedRoadHeightBlock extends Block {
    public static final MapCodec<FixedRoadHeightBlock> CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Codec.intRange(1, 15).fieldOf("height").forGetter(block -> block.height),
                    createSettingsCodec()
            ).apply(instance, FixedRoadHeightBlock::new));

    private final int height;
    private final VoxelShape shape;

    public FixedRoadHeightBlock(int height, Settings settings) {
        super(settings);
        this.height = Math.clamp(height, 1, 15);
        this.shape = createCuboidShape(0.0, 0.0, 0.0, 16.0, this.height, 16.0);
    }

    public int height() {
        return height;
    }

    @Override
    protected MapCodec<? extends Block> getCodec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world,
                                         BlockPos pos, ShapeContext context) {
        return shape;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockView world,
                                           BlockPos pos, ShapeContext context) {
        return shape;
    }

    @Override
    protected int getOpacity(BlockState state) {
        return 0;
    }

    @Override
    protected boolean isTransparent(BlockState state) {
        return true;
    }
}

package net.evarius.tnadditions.block.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;

public final class HydrantBlock extends Block {
    public static final MapCodec<HydrantBlock> CODEC = createCodec(HydrantBlock::new);
    private static final VoxelShape SHAPE = Block.createCuboidShape(5.2, 0, 5.2, 10.8, 15.75, 10.8);

    public HydrantBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected MapCodec<? extends Block> getCodec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getOutlineShape(
            BlockState state,
            BlockView world,
            BlockPos pos,
            ShapeContext context
    ) {
        return SHAPE;
    }
}

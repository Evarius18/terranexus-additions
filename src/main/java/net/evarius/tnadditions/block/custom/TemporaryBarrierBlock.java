package net.evarius.tnadditions.block.custom;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;

/**
 * Directional base block for temporary construction barriers. The profiles
 * describe the supplied models closely enough to keep their visible frame,
 * feet and extended multi-block dimensions physically usable.
 */
public class TemporaryBarrierBlock extends HorizontalFacingBlock {
    public static final MapCodec<TemporaryBarrierBlock> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Profile.CODEC.fieldOf("profile").forGetter(block -> block.profile),
                    createSettingsCodec()
            ).apply(instance, TemporaryBarrierBlock::new)
    );

    private final Profile profile;

    public TemporaryBarrierBlock(Profile profile, Settings settings) {
        super(settings);
        this.profile = profile;
        setDefaultState(getStateManager().getDefaultState().with(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends HorizontalFacingBlock> getCodec() {
        return CODEC;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        return getDefaultState().with(FACING, context.getHorizontalPlayerFacing().getOpposite());
    }

    @Override
    protected BlockState rotate(BlockState state, BlockRotation rotation) {
        return state.with(FACING, rotation.rotate(state.get(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.rotate(mirror.getRotation(state.get(FACING)));
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return profile.getShape(state.get(FACING));
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return profile.getShape(state.get(FACING));
    }

    public enum Profile implements StringIdentifiable {
        BARKE("barke",
                cuboid(4, 0, 1.2, 12, 1.5, 15),
                cuboid(4, 1, 7, 12, 28, 9)),
        BARKE_LIGHT("barke_light",
                cuboid(4, 0, 1.2, 12, 1.5, 15),
                cuboid(4, 1, 7, 12, 30.5, 9)),
        BARKE_FOOT("barke_foot",
                cuboid(4, 0, 1.2, 12, 1.5, 15)),
        LARGE_BARKE("large_barke",
                cuboid(-16, 0, 1.2, -8, 1.5, 15),
                cuboid(24, 0, 1.2, 32, 1.5, 15),
                cuboid(-16, 1, 7, 32, 22, 9)),
        LARGE_BARKE_LIGHT("large_barke_light",
                cuboid(-16, 0, 1.2, -8, 1.5, 15),
                cuboid(24, 0, 1.2, 32, 1.5, 15),
                cuboid(-16, 1, 7, 32, 27.5, 9)),
        CONSTRUCTION_FENCE("construction_fence",
                cuboid(-16, 0, 1.2, -8, 1.5, 15),
                cuboid(24, 0, 1.2, 32, 1.5, 15),
                cuboid(-16, 1, 7.5, 32, 32, 8.5)),
        WARNING_LIGHT("warning_light",
                cuboid(5, 0, 6.95, 11, 7.5, 9.05));

        private static final Codec<Profile> CODEC = StringIdentifiable.createCodec(Profile::values);

        private final String id;
        private final VoxelShape alongX;
        private final VoxelShape alongZ;

        Profile(String id, Cuboid... cuboids) {
            this.id = id;
            this.alongX = createShape(cuboids);
            this.alongZ = createShape(rotateClockwise(cuboids));
        }

        private VoxelShape getShape(Direction facing) {
            return facing.getAxis() == Direction.Axis.Z ? alongX : alongZ;
        }

        @Override
        public String asString() {
            return id;
        }

        private static Cuboid cuboid(
                double minX,
                double minY,
                double minZ,
                double maxX,
                double maxY,
                double maxZ
        ) {
            return new Cuboid(minX, minY, minZ, maxX, maxY, maxZ);
        }

        private static Cuboid[] rotateClockwise(Cuboid[] cuboids) {
            Cuboid[] rotated = new Cuboid[cuboids.length];
            for (int index = 0; index < cuboids.length; index++) {
                Cuboid cuboid = cuboids[index];
                rotated[index] = new Cuboid(
                        16 - cuboid.maxZ,
                        cuboid.minY,
                        cuboid.minX,
                        16 - cuboid.minZ,
                        cuboid.maxY,
                        cuboid.maxX
                );
            }
            return rotated;
        }

        private static VoxelShape createShape(Cuboid[] cuboids) {
            VoxelShape result = VoxelShapes.empty();
            for (Cuboid cuboid : cuboids) {
                result = VoxelShapes.union(result, Block.createCuboidShape(
                        cuboid.minX,
                        cuboid.minY,
                        cuboid.minZ,
                        cuboid.maxX,
                        cuboid.maxY,
                        cuboid.maxZ
                ));
            }
            return result;
        }

        private record Cuboid(
                double minX,
                double minY,
                double minZ,
                double maxX,
                double maxY,
                double maxZ
        ) {
        }
    }
}

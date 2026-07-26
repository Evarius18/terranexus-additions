package net.evarius.tnadditions.marking.support;

import net.evarius.tnadditions.config.RoadMarkingSupportConfig;
import net.evarius.tnadditions.marking.RoadMarking;
import net.evarius.tnadditions.marking.network.RoadMarkingNetworking;
import net.evarius.tnadditions.marking.spline.CatmullRomSpline;
import net.evarius.tnadditions.marking.spline.CurvePointPreprocessor;
import net.evarius.tnadditions.marking.storage.RoadMarkingState;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;

/**
 * Periodically validates generated marking paths against actual collision
 * surfaces. Unloaded chunks are skipped and never force chunk loads.
 */
public final class RoadMarkingSupportManager {
    private static final double SURFACE_TOLERANCE = 0.16;
    private static final int MAX_CHECKED_SAMPLES = 4096;

    public static void register() {
        ServerTickEvents.END_WORLD_TICK.register(RoadMarkingSupportManager::tick);
    }

    private static void tick(ServerWorld world) {
        if (!RoadMarkingSupportConfig.isEnabled()
                || world.getTime() % RoadMarkingSupportConfig.checkIntervalTicks() != 0) return;

        RoadMarkingState state = RoadMarkingState.get(world);
        for (RoadMarking marking : state.all()) {
            boolean supported = hasSupport(world, marking);
            switch (RoadMarkingSupportConfig.behavior()) {
                case REMOVE -> {
                    if (!supported && state.remove(marking.id())) {
                        RoadMarkingNetworking.broadcastDelete(world, marking.id());
                    } else if (supported && !marking.enabled()) {
                        RoadMarking updated = marking.withEnabled(true);
                        state.put(updated);
                        RoadMarkingNetworking.broadcastUpsert(world, updated);
                    }
                }
                case DISABLE -> {
                    if (marking.enabled() != supported) {
                        RoadMarking updated = marking.withEnabled(supported);
                        state.put(updated);
                        RoadMarkingNetworking.broadcastUpsert(world, updated);
                    }
                }
                case ADAPT -> {
                    if (!supported) {
                        RoadMarking adapted = adaptToSurface(world, marking);
                        if (adapted != null
                                && !adapted.controlPoints().equals(marking.controlPoints())
                                && hasSupport(world, adapted)) {
                            state.put(adapted);
                            RoadMarkingNetworking.broadcastUpsert(world, adapted);
                        } else if (state.remove(marking.id())) {
                            RoadMarkingNetworking.broadcastDelete(world, marking.id());
                        }
                    } else if (!marking.enabled()) {
                        RoadMarking updated = marking.withEnabled(true);
                        state.put(updated);
                        RoadMarkingNetworking.broadcastUpsert(world, updated);
                    }
                }
            }
        }
    }

    public static boolean hasSupport(ServerWorld world, RoadMarking marking) {
        List<Vec3d> curvePoints = CurvePointPreprocessor.roundCorners(
                marking.controlPoints(), marking.style().cornerRadius());
        var samples = new CatmullRomSpline(curvePoints).sample(RoadMarkingSupportConfig.sampleSpacing());
        int stride = Math.max(1, (int) Math.ceil(samples.size() / (double) MAX_CHECKED_SAMPLES));
        for (int i = 0; i < samples.size(); i += stride) {
            double surface = surfaceAt(world, samples.get(i).position());
            if (Double.isNaN(surface)) continue; // Unloaded: do not load or invalidate.
            if (!Double.isFinite(surface)
                    || Math.abs(surface - samples.get(i).position().y) > SURFACE_TOLERANCE) return false;
        }
        return true;
    }

    private static RoadMarking adaptToSurface(ServerWorld world, RoadMarking marking) {
        List<Vec3d> adapted = new ArrayList<>(marking.controlPoints().size());
        for (Vec3d point : marking.controlPoints()) {
            OptionalDouble surface = findNearestSurface(world, point, RoadMarkingSupportConfig.maxAdaptDistance());
            if (surface.isEmpty()) return null;
            adapted.add(new Vec3d(point.x, surface.getAsDouble(), point.z));
        }
        return new RoadMarking(marking.id(), marking.type(), adapted, marking.style(),
                marking.revision() + 1, true);
    }

    private static OptionalDouble findNearestSurface(ServerWorld world, Vec3d point, double range) {
        double bestSurface = 0.0;
        double bestDistance = Double.MAX_VALUE;
        int minY = (int) Math.floor(point.y - range);
        int maxY = (int) Math.floor(point.y + range);
        for (int y = minY; y <= maxY; y++) {
            BlockPos pos = BlockPos.ofFloored(point.x, y, point.z);
            if (!isChunkLoaded(world, pos)) continue;
            VoxelShape shape = world.getBlockState(pos).getCollisionShape(world, pos);
            if (shape.isEmpty()) continue;
            double surface = pos.getY() + shape.getMax(Direction.Axis.Y);
            double distance = Math.abs(surface - point.y);
            if (distance <= range && distance < bestDistance) {
                bestDistance = distance;
                bestSurface = surface + 0.01;
            }
        }
        return bestDistance == Double.MAX_VALUE ? OptionalDouble.empty() : OptionalDouble.of(bestSurface);
    }

    private static double surfaceAt(ServerWorld world, Vec3d point) {
        BlockPos pos = BlockPos.ofFloored(point.x, point.y - 0.02, point.z);
        if (!isChunkLoaded(world, pos)) return Double.NaN;
        VoxelShape shape = world.getBlockState(pos).getCollisionShape(world, pos);
        if (shape.isEmpty()) return Double.NEGATIVE_INFINITY;
        return pos.getY() + shape.getMax(Direction.Axis.Y) + 0.01;
    }

    private static boolean isChunkLoaded(ServerWorld world, BlockPos pos) {
        return world.getChunkManager().isChunkLoaded(pos.getX() >> 4, pos.getZ() >> 4);
    }

    private RoadMarkingSupportManager() {
    }
}

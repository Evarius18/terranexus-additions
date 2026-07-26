package net.evarius.tnadditions.client.marking;

import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Direction;

import java.util.List;

/**
 * Non-persistent construction aids. They affect newly positioned control
 * points, while the resulting exact coordinates remain normal world data.
 */
public final class MarkingPlacementSettings {
    public enum AxisLock {
        FREE, WORLD_X, WORLD_Z, FIRST_DIRECTION;

        public AxisLock next() {
            AxisLock[] values = values();
            return values[(ordinal() + 1) % values.length];
        }
    }

    private static AxisLock axisLock = AxisLock.FREE;
    private static double gridSize = 0.25;
    private static double angleSnapDegrees;
    private static double exactLength;
    private static double exactAngleDegrees = Double.NaN;

    public static Vec3d apply(Vec3d raw, List<Vec3d> existingPoints) {
        double x = raw.x;
        double z = raw.z;
        if (gridSize > 0.0) {
            x = Math.round(x / gridSize) * gridSize;
            z = Math.round(z / gridSize) * gridSize;
        }
        if (existingPoints.isEmpty()) return new Vec3d(x, raw.y, z);

        Vec3d origin = existingPoints.getLast();
        Vec3d delta = new Vec3d(x - origin.x, 0.0, z - origin.z);
        double length = Math.sqrt(delta.x * delta.x + delta.z * delta.z);
        if (length < 1.0E-6) return new Vec3d(x, raw.y, z);

        double angle = Math.toDegrees(Math.atan2(delta.z, delta.x));
        switch (axisLock) {
            case WORLD_X -> angle = delta.x < 0.0 ? 180.0 : 0.0;
            case WORLD_Z -> angle = delta.z < 0.0 ? -90.0 : 90.0;
            case FIRST_DIRECTION -> {
                if (existingPoints.size() >= 2) {
                    Vec3d first = existingPoints.get(1).subtract(existingPoints.getFirst());
                    angle = Math.toDegrees(Math.atan2(first.z, first.x));
                    Vec3d direction = new Vec3d(Math.cos(Math.toRadians(angle)), 0.0,
                            Math.sin(Math.toRadians(angle)));
                    if (delta.dotProduct(direction) < 0.0) angle += 180.0;
                }
            }
            default -> {
            }
        }

        if (Double.isFinite(exactAngleDegrees)) {
            angle = exactAngleDegrees;
        } else if (angleSnapDegrees > 0.0 && axisLock == AxisLock.FREE) {
            angle = Math.round(angle / angleSnapDegrees) * angleSnapDegrees;
        }
        if (exactLength > 0.0) length = exactLength;

        double radians = Math.toRadians(angle);
        return new Vec3d(
                origin.x + Math.cos(radians) * length,
                raw.y,
                origin.z + Math.sin(radians) * length
        );
    }

    public static Vec3d snapToSurfaceGrid(Vec3d raw, Direction side) {
        if (gridSize <= 0.0) return raw;
        double x = raw.x;
        double y = raw.y;
        double z = raw.z;
        switch (side.getAxis()) {
            case Y -> {
                x = Math.round(x / gridSize) * gridSize;
                z = Math.round(z / gridSize) * gridSize;
            }
            case Z -> {
                x = Math.round(x / gridSize) * gridSize;
                y = Math.round(y / gridSize) * gridSize;
            }
            case X -> {
                z = Math.round(z / gridSize) * gridSize;
                y = Math.round(y / gridSize) * gridSize;
            }
        }
        return new Vec3d(x, y, z);
    }

    public static AxisLock axisLock() { return axisLock; }
    public static void cycleAxisLock() { axisLock = axisLock.next(); }
    public static double gridSize() { return gridSize; }
    public static void cycleGrid() {
        gridSize = gridSize == 0.0 ? 0.25 : gridSize == 0.25 ? 0.5 : gridSize == 0.5 ? 1.0 : 0.0;
    }
    public static double angleSnapDegrees() { return angleSnapDegrees; }
    public static void cycleAngleSnap() {
        angleSnapDegrees = angleSnapDegrees == 0.0 ? 15.0
                : angleSnapDegrees == 15.0 ? 30.0
                : angleSnapDegrees == 30.0 ? 45.0
                : angleSnapDegrees == 45.0 ? 90.0 : 0.0;
    }
    public static double exactLength() { return exactLength; }
    public static void setExactLength(double value) { exactLength = Math.clamp(value, 0.0, 4096.0); }
    public static double exactAngleDegrees() { return exactAngleDegrees; }
    public static void setExactAngleDegrees(double value) {
        exactAngleDegrees = Double.isFinite(value) ? value : Double.NaN;
    }

    private MarkingPlacementSettings() {
    }
}

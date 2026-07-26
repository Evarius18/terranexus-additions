package net.evarius.tnadditions.marking.spline;

import net.minecraft.util.math.Vec3d;

public record SplineSample(
        Vec3d position,
        Vec3d tangent,
        Vec3d normal,
        double distance,
        double curvature
) {
}

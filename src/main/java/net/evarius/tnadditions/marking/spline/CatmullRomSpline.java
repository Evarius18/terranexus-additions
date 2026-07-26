package net.evarius.tnadditions.marking.spline;

import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

/**
 * Centripedal Catmull-Rom interpolation with distance-based resampling. It
 * passes through every control point and avoids loops at sharp corners better
 * than the uniform variant.
 */
public final class CatmullRomSpline {
    private static final double ALPHA = 0.5;
    private final List<Vec3d> points;

    public CatmullRomSpline(List<Vec3d> points) {
        if (points.size() < 2) {
            throw new IllegalArgumentException("At least two control points are required");
        }
        this.points = List.copyOf(points);
    }

    public List<SplineSample> sample(double targetSpacing) {
        double spacing = Math.clamp(targetSpacing, 0.025, 2.0);
        List<Vec3d> dense = new ArrayList<>();
        int segmentSteps = Math.max(12, (int) Math.ceil(1.0 / spacing) * 4);
        for (int segment = 0; segment < points.size() - 1; segment++) {
            for (int step = segment == 0 ? 0 : 1; step <= segmentSteps; step++) {
                dense.add(position(segment, step / (double) segmentSteps));
            }
        }
        return resample(dense, spacing);
    }

    private Vec3d position(int segment, double u) {
        Vec3d p1 = points.get(segment);
        Vec3d p2 = points.get(segment + 1);
        boolean closed = points.size() >= 4 && points.getFirst().squaredDistanceTo(points.getLast()) < 1.0E-8;
        Vec3d p0 = segment > 0 ? points.get(segment - 1)
                : closed ? points.get(points.size() - 2) : p1.multiply(2.0).subtract(p2);
        Vec3d p3 = segment + 2 < points.size() ? points.get(segment + 2)
                : closed ? points.get(1) : p2.multiply(2.0).subtract(p1);

        double t0 = 0.0;
        double t1 = t0 + knot(p0, p1);
        double t2 = t1 + knot(p1, p2);
        double t3 = t2 + knot(p2, p3);
        double t = t1 + (t2 - t1) * u;
        Vec3d a1 = interpolate(p0, p1, t0, t1, t);
        Vec3d a2 = interpolate(p1, p2, t1, t2, t);
        Vec3d a3 = interpolate(p2, p3, t2, t3, t);
        Vec3d b1 = interpolate(a1, a2, t0, t2, t);
        Vec3d b2 = interpolate(a2, a3, t1, t3, t);
        return interpolate(b1, b2, t1, t2, t);
    }

    private static double knot(Vec3d a, Vec3d b) {
        return Math.max(1.0E-5, Math.pow(a.distanceTo(b), ALPHA));
    }

    private static Vec3d interpolate(Vec3d a, Vec3d b, double ta, double tb, double t) {
        double span = Math.max(1.0E-6, tb - ta);
        return a.multiply((tb - t) / span).add(b.multiply((t - ta) / span));
    }

    private static List<SplineSample> resample(List<Vec3d> dense, double spacing) {
        List<Vec3d> positions = new ArrayList<>();
        positions.add(dense.getFirst());
        Vec3d previous = dense.getFirst();
        double carry = 0.0;
        for (int i = 1; i < dense.size(); i++) {
            Vec3d target = dense.get(i);
            double distance = previous.distanceTo(target);
            while (carry + distance >= spacing && distance > 1.0E-7) {
                double factor = (spacing - carry) / distance;
                previous = previous.lerp(target, factor);
                positions.add(previous);
                distance = previous.distanceTo(target);
                carry = 0.0;
            }
            carry += distance;
            previous = target;
        }
        if (!positions.getLast().isInRange(dense.getLast(), spacing * 0.25)) {
            positions.add(dense.getLast());
        }

        List<SplineSample> result = new ArrayList<>(positions.size());
        double traveled = 0.0;
        Vec3d lastTangent = Vec3d.ZERO;
        for (int i = 0; i < positions.size(); i++) {
            Vec3d before = positions.get(Math.max(0, i - 1));
            Vec3d after = positions.get(Math.min(positions.size() - 1, i + 1));
            Vec3d tangent = after.subtract(before).normalize();
            if (tangent.lengthSquared() < 1.0E-8) tangent = lastTangent;
            Vec3d normal = new Vec3d(-tangent.z, 0.0, tangent.x).normalize();
            if (i > 0) traveled += positions.get(i - 1).distanceTo(positions.get(i));
            double curvature = i == 0 || lastTangent.lengthSquared() == 0.0
                    ? 0.0 : tangent.subtract(lastTangent).length() / Math.max(spacing, 1.0E-4);
            result.add(new SplineSample(positions.get(i), tangent, normal, traveled, curvature));
            lastTangent = tangent;
        }
        return result;
    }
}

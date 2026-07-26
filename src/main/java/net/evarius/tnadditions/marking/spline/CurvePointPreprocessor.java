package net.evarius.tnadditions.marking.spline;

import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

/**
 * Converts sharp polyline corners into a compact pair of approach/departure
 * points. The Catmull-Rom sampler then creates a smooth road-style transition
 * without requiring users to place many intermediate points.
 */
public final class CurvePointPreprocessor {
    public static List<Vec3d> roundCorners(List<Vec3d> points, double radius) {
        if (radius <= 1.0E-4 || points.size() < 3) return points;
        boolean closed = points.size() >= 4
                && points.getFirst().squaredDistanceTo(points.getLast()) < 1.0E-8;
        if (closed) return roundClosed(points, radius);

        List<Vec3d> rounded = new ArrayList<>();
        rounded.add(points.getFirst());
        for (int i = 1; i < points.size() - 1; i++) {
            appendCorner(rounded, points.get(i - 1), points.get(i), points.get(i + 1), radius);
        }
        rounded.add(points.getLast());
        return List.copyOf(rounded);
    }

    private static List<Vec3d> roundClosed(List<Vec3d> points, double radius) {
        int unique = points.size() - 1;
        List<Vec3d> rounded = new ArrayList<>(unique * 2 + 1);
        for (int i = 0; i < unique; i++) {
            Vec3d previous = points.get((i - 1 + unique) % unique);
            Vec3d current = points.get(i);
            Vec3d next = points.get((i + 1) % unique);
            appendCorner(rounded, previous, current, next, radius);
        }
        if (!rounded.isEmpty()) rounded.add(rounded.getFirst());
        return List.copyOf(rounded);
    }

    private static void appendCorner(List<Vec3d> output, Vec3d previous, Vec3d corner,
                                     Vec3d next, double radius) {
        Vec3d incoming = corner.subtract(previous);
        Vec3d outgoing = next.subtract(corner);
        double incomingLength = incoming.length();
        double outgoingLength = outgoing.length();
        if (incomingLength < 1.0E-5 || outgoingLength < 1.0E-5) {
            output.add(corner);
            return;
        }
        double cut = Math.min(radius, Math.min(incomingLength, outgoingLength) * 0.45);
        output.add(corner.subtract(incoming.normalize().multiply(cut)));
        output.add(corner.add(outgoing.normalize().multiply(cut)));
    }

    private CurvePointPreprocessor() {
    }
}

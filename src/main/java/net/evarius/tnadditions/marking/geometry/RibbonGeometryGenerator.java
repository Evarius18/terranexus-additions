package net.evarius.tnadditions.marking.geometry;

import net.evarius.tnadditions.marking.MarkingStyle;
import net.evarius.tnadditions.marking.MarkingType;
import net.evarius.tnadditions.marking.spline.SplineSample;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

public final class RibbonGeometryGenerator implements MarkingType {
    public enum Pattern {
        SOLID, DASHED, DOUBLE, DOUBLE_DASHED, STOP, HATCH, PARKING, CROSSWALK, SYMBOL, ARROW, TURN_ARROW
    }

    private final Identifier id;
    private final Pattern pattern;

    public RibbonGeometryGenerator(Identifier id, Pattern pattern) {
        this.id = id;
        this.pattern = pattern;
    }

    @Override
    public Identifier id() {
        return id;
    }

    @Override
    public MarkingGeometry generate(List<SplineSample> samples, MarkingStyle style) {
        List<MarkingGeometry.Quad> quads = new ArrayList<>();
        if (samples.size() < 2) return new MarkingGeometry(quads, Box.of(Vec3d.ZERO, 0, 0, 0), 0);
        if (Math.abs(style.lateralOffset()) > 1.0E-6) {
            samples = samples.stream().map(sample -> new SplineSample(
                    sample.position().add(sample.normal().multiply(style.lateralOffset())),
                    sample.tangent(), sample.normal(), sample.distance(), sample.curvature()
            )).toList();
        }
        switch (pattern) {
            case DOUBLE, DOUBLE_DASHED -> {
                double offset = style.width() * 0.9;
                appendRibbon(quads, samples, style, -offset, pattern == Pattern.DOUBLE_DASHED);
                appendRibbon(quads, samples, style, offset, pattern == Pattern.DOUBLE_DASHED);
            }
            case STOP -> appendCrossBar(quads, samples.getLast(), Math.max(style.width(), 0.4), 3.0);
            case CROSSWALK -> appendCrosswalk(quads, samples, style);
            case HATCH -> appendHatch(quads, samples, style);
            case PARKING -> appendParking(quads, samples, style);
            case SYMBOL, ARROW, TURN_ARROW -> {
                appendRibbon(quads, samples, style, 0.0, true);
                appendSymbol(quads, samples.get(samples.size() / 2), style, pattern, id.getPath());
            }
            default -> appendRibbon(quads, samples, style, 0.0, pattern == Pattern.DASHED);
        }
        Box bounds = bounds(quads);
        return new MarkingGeometry(List.copyOf(quads), bounds, samples.getLast().distance());
    }

    private static void appendRibbon(List<MarkingGeometry.Quad> out, List<SplineSample> samples,
                                     MarkingStyle style, double laneOffset, boolean dashed) {
        double half = style.width() * 0.5;
        double period = style.dashLength() + style.gapLength();
        for (int i = 1; i < samples.size(); i++) {
            SplineSample a = samples.get(i - 1);
            SplineSample b = samples.get(i);
            double mid = (a.distance() + b.distance()) * 0.5;
            if (dashed && period > 0.0 && mid % period > style.dashLength()) continue;
            Vec3d ac = a.position().add(a.normal().multiply(laneOffset)).add(0, style.heightOffset(), 0);
            Vec3d bc = b.position().add(b.normal().multiply(laneOffset)).add(0, style.heightOffset(), 0);
            out.add(new MarkingGeometry.Quad(
                    ac.subtract(a.normal().multiply(half)),
                    ac.add(a.normal().multiply(half)),
                    bc.add(b.normal().multiply(half)),
                    bc.subtract(b.normal().multiply(half))
            ));
        }
    }

    private static void appendCrossBar(List<MarkingGeometry.Quad> out, SplineSample sample, double depth, double width) {
        Vec3d c = sample.position().add(0, 0.013, 0);
        Vec3d along = sample.tangent().multiply(depth * 0.5);
        Vec3d side = sample.normal().multiply(width * 0.5);
        out.add(new MarkingGeometry.Quad(c.subtract(along).subtract(side), c.subtract(along).add(side),
                c.add(along).add(side), c.add(along).subtract(side)));
    }

    private static void appendCrosswalk(List<MarkingGeometry.Quad> out, List<SplineSample> samples, MarkingStyle style) {
        for (int i = 0; i < samples.size(); i += Math.max(1, samples.size() / 8)) {
            appendCrossBar(out, samples.get(i), 0.45, Math.max(3.0, style.width() * 12.0));
        }
    }

    private static void appendHatch(List<MarkingGeometry.Quad> out, List<SplineSample> samples, MarkingStyle style) {
        appendRibbon(out, samples, style, -Math.max(0.5, style.width() * 4), false);
        appendRibbon(out, samples, style, Math.max(0.5, style.width() * 4), false);
        for (int i = 0; i < samples.size(); i += Math.max(2, samples.size() / 12)) {
            appendCrossBar(out, samples.get(i), style.width(), Math.max(1.0, style.width() * 8));
        }
    }

    private static void appendParking(List<MarkingGeometry.Quad> out, List<SplineSample> samples, MarkingStyle style) {
        appendRibbon(out, samples, style, -1.25, false);
        appendRibbon(out, samples, style, 1.25, false);
        for (SplineSample sample : samples) {
            if (sample.distance() % 5.0 < 0.15) appendCrossBar(out, sample, style.width(), 2.5);
        }
    }

    private static void appendSymbol(List<MarkingGeometry.Quad> out, SplineSample sample,
                                     MarkingStyle style, Pattern pattern, String id) {
        if ("bus_lane".equals(id)) {
            appendBus(out, sample, style);
            return;
        }
        if ("bike_lane".equals(id)) {
            appendBike(out, sample, style);
            return;
        }
        appendArrow(out, sample, style, pattern == Pattern.TURN_ARROW);
    }

    private static void appendArrow(List<MarkingGeometry.Quad> out, SplineSample sample,
                                    MarkingStyle style, boolean turn) {
        Vec3d center = sample.position().add(0, style.heightOffset(), 0);
        Vec3d tangent = sample.tangent();
        Vec3d normal = sample.normal();
        appendOrientedRect(out, center.subtract(tangent.multiply(0.45)), tangent, normal, 1.5, 0.24);
        Vec3d head = center.add(tangent.multiply(0.65));
        if (turn) head = head.add(normal.multiply(0.45));
        Vec3d back = head.subtract(tangent.multiply(0.7));
        out.add(new MarkingGeometry.Quad(
                head,
                back.add(normal.multiply(0.6)),
                back.subtract(normal.multiply(0.6)),
                head
        ));
    }

    private static void appendBus(List<MarkingGeometry.Quad> out, SplineSample sample, MarkingStyle style) {
        Vec3d c = sample.position().add(0, style.heightOffset(), 0);
        Vec3d t = sample.tangent();
        Vec3d n = sample.normal();
        // Compact stencil-style BUS glyph built from rectangles.
        for (double offset : new double[]{-1.0, 0.0, 1.0}) {
            appendOrientedRect(out, c.add(n.multiply(offset)), t, n, 1.6, 0.16);
        }
        appendOrientedRect(out, c.add(t.multiply(0.72)), n, t, 2.2, 0.16);
        appendOrientedRect(out, c.subtract(t.multiply(0.72)), n, t, 2.2, 0.16);
        appendOrientedRect(out, c, n, t, 1.05, 0.14);
    }

    private static void appendBike(List<MarkingGeometry.Quad> out, SplineSample sample, MarkingStyle style) {
        Vec3d c = sample.position().add(0, style.heightOffset(), 0);
        Vec3d t = sample.tangent();
        Vec3d n = sample.normal();
        Vec3d front = c.add(t.multiply(0.65));
        Vec3d rear = c.subtract(t.multiply(0.65));
        for (int i = 0; i < 8; i++) {
            double angle = Math.PI * i / 4.0;
            Vec3d axis = t.multiply(Math.cos(angle)).add(n.multiply(Math.sin(angle)));
            appendOrientedRect(out, front, axis, new Vec3d(-axis.z, 0, axis.x), 0.75, 0.08);
            appendOrientedRect(out, rear, axis, new Vec3d(-axis.z, 0, axis.x), 0.75, 0.08);
        }
        appendOrientedRect(out, c, t, n, 1.3, 0.10);
        appendOrientedRect(out, c.add(n.multiply(0.18)), n, t, 0.7, 0.10);
    }

    private static void appendOrientedRect(List<MarkingGeometry.Quad> out, Vec3d center,
                                           Vec3d along, Vec3d side, double length, double width) {
        Vec3d a = along.normalize().multiply(length * 0.5);
        Vec3d s = side.normalize().multiply(width * 0.5);
        out.add(new MarkingGeometry.Quad(
                center.subtract(a).subtract(s), center.subtract(a).add(s),
                center.add(a).add(s), center.add(a).subtract(s)
        ));
    }

    private static Box bounds(List<MarkingGeometry.Quad> quads) {
        if (quads.isEmpty()) return Box.of(Vec3d.ZERO, 0, 0, 0);
        Vec3d p = quads.getFirst().a();
        double minX = p.x, minY = p.y, minZ = p.z, maxX = p.x, maxY = p.y, maxZ = p.z;
        for (MarkingGeometry.Quad quad : quads) {
            for (Vec3d v : List.of(quad.a(), quad.b(), quad.c(), quad.d())) {
                minX = Math.min(minX, v.x); minY = Math.min(minY, v.y); minZ = Math.min(minZ, v.z);
                maxX = Math.max(maxX, v.x); maxY = Math.max(maxY, v.y); maxZ = Math.max(maxZ, v.z);
            }
        }
        return new Box(minX, minY, minZ, maxX, maxY, maxZ);
    }
}

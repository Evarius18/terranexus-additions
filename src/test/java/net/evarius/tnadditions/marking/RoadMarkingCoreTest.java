package net.evarius.tnadditions.marking;

import net.evarius.tnadditions.marking.spline.CatmullRomSpline;
import net.evarius.tnadditions.marking.spline.CurvePointPreprocessor;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

import java.util.List;
import java.util.UUID;

/**
 * Dependency-free verification executable so the mod does not need to ship or
 * download a unit-test framework.
 */
public final class RoadMarkingCoreTest {
    public static void main(String[] args) {
        MarkingTypes.registerDefaults();
        List<Vec3d> points = List.of(
                new Vec3d(0, 64, 0),
                new Vec3d(4, 64, 2),
                new Vec3d(8, 64.5, -2),
                new Vec3d(12, 64, 0)
        );
        var samples = new CatmullRomSpline(points).sample(0.15);
        require(samples.size() > points.size(), "spline was not subdivided");
        require(samples.getFirst().position().distanceTo(points.getFirst()) < 1.0E-6, "start point drifted");
        require(samples.getLast().position().distanceTo(points.getLast()) < 1.0E-6, "end point drifted");
        double distance = -1.0;
        for (var sample : samples) {
            require(sample.distance() >= distance, "arc length is not monotonic");
            require(Double.isFinite(sample.curvature()), "curvature is not finite");
            require(Math.abs(sample.tangent().length() - 1.0) < 1.0E-4, "tangent is not normalized");
            distance = sample.distance();
        }

        var loop = new CatmullRomSpline(List.of(
                new Vec3d(0, 64, 0), new Vec3d(4, 64, 0), new Vec3d(4, 64, 4),
                new Vec3d(0, 64, 4), new Vec3d(0, 64, 0)
        )).sample(0.1);
        require(loop.getFirst().tangent().dotProduct(loop.getLast().tangent()) > 0.85,
                "closed roundabout spline has a visible seam");
        var rounded = CurvePointPreprocessor.roundCorners(points, 1.5);
        require(rounded.size() > points.size(), "corner radius did not add transition points");
        require(rounded.getFirst().equals(points.getFirst()), "rounding moved the start point");
        require(rounded.getLast().equals(points.getLast()), "rounding moved the end point");

        for (MarkingType type : MarkingTypes.values()) {
            var geometry = type.generate(samples, MarkingStyle.DEFAULT);
            require(!geometry.quads().isEmpty(), "empty geometry for " + type.id());
            require(geometry.length() > 0.0, "invalid length for " + type.id());
        }
        var offsetStyle = new MarkingStyle(
                MarkingStyle.DEFAULT.width(), MarkingStyle.DEFAULT.color(), MarkingStyle.DEFAULT.material(),
                1.0F, 0.0F, 0.0F, 3.0, 6.0, 0.0125, 1.0, 0.0, 0, false
        );
        var centeredQuad = MarkingTypes.get(MarkingTypes.SOLID)
                .generate(samples, MarkingStyle.DEFAULT).quads().getFirst();
        var offsetQuad = MarkingTypes.get(MarkingTypes.SOLID)
                .generate(samples, offsetStyle).quads().getFirst();
        Vec3d centered = centeredQuad.a().add(centeredQuad.b()).add(centeredQuad.c()).add(centeredQuad.d()).multiply(0.25);
        Vec3d offset = offsetQuad.a().add(offsetQuad.b()).add(offsetQuad.c()).add(offsetQuad.d()).multiply(0.25);
        require(offset.subtract(centered).dotProduct(samples.getFirst().normal()) > 0.9,
                "lateral spline offset was not applied along the normal");

        RoadMarking original = new RoadMarking(
                UUID.randomUUID(), Identifier.of("tnadditions", "double_dashed"),
                points, new MarkingStyle(0.18, 0xFFFFD21F, "reflective", 0.8F,
                0.15F, 0.2F, 2.5, 4.5, 0.02, -0.5, 2.0, 3, false), 7
        );
        RoadMarking restored = RoadMarking.fromNbt(original.toNbt());
        require(restored.id().equals(original.id()), "UUID did not survive NBT");
        require(restored.controlPoints().equals(original.controlPoints()), "control points did not survive NBT");
        require(restored.style().equals(original.style()), "style did not survive NBT");
        require(restored.revision() == original.revision(), "revision did not survive NBT");
        require(restored.enabled(), "enabled state did not survive NBT");
        var disabled = original.withEnabled(false);
        require(!RoadMarking.fromNbt(disabled.toNbt()).enabled(), "disabled state did not survive NBT");
        var legacyMarkingNbt = original.toNbt();
        legacyMarkingNbt.remove("enabled");
        require(RoadMarking.fromNbt(legacyMarkingNbt).enabled(),
                "legacy marking without enabled flag must remain visible");
        var legacyStyleNbt = MarkingStyle.DEFAULT.toNbt();
        legacyStyleNbt.remove("lateral_offset");
        legacyStyleNbt.remove("corner_radius");
        MarkingStyle legacyStyle = MarkingStyle.fromNbt(legacyStyleNbt);
        require(legacyStyle.lateralOffset() == 0.0, "legacy offset default is incompatible");
        require(legacyStyle.cornerRadius() == 0.0, "legacy radius default is incompatible");
        System.out.println("Road marking core checks passed: " + MarkingTypes.values().size() + " types");
    }

    private static void require(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }

    private RoadMarkingCoreTest() {
    }
}

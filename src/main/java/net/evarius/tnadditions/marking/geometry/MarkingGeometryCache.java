package net.evarius.tnadditions.marking.geometry;

import net.evarius.tnadditions.marking.MarkingTypes;
import net.evarius.tnadditions.marking.RoadMarking;
import net.evarius.tnadditions.marking.spline.CatmullRomSpline;
import net.evarius.tnadditions.marking.spline.CurvePointPreprocessor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class MarkingGeometryCache {
    private final Map<UUID, Entry> cache = new HashMap<>();

    public MarkingGeometry get(RoadMarking marking) {
        Entry current = cache.get(marking.id());
        if (current != null && current.revision == marking.revision()) return current.geometry;
        double spacing = Math.clamp(marking.style().width() * 0.5, 0.05, 0.35);
        var curvePoints = CurvePointPreprocessor.roundCorners(
                marking.controlPoints(), marking.style().cornerRadius());
        var samples = new CatmullRomSpline(curvePoints).sample(spacing);
        MarkingGeometry geometry = MarkingTypes.get(marking.type()).generate(samples, marking.style());
        cache.put(marking.id(), new Entry(marking.revision(), geometry));
        return geometry;
    }

    public void invalidate(UUID id) {
        cache.remove(id);
    }

    public void retain(Iterable<UUID> ids) {
        java.util.Set<UUID> keep = new java.util.HashSet<>();
        ids.forEach(keep::add);
        cache.keySet().retainAll(keep);
    }

    private record Entry(long revision, MarkingGeometry geometry) {
    }
}

package net.evarius.tnadditions.client.marking;

import net.evarius.tnadditions.marking.MarkingStyle;
import net.evarius.tnadditions.marking.MarkingTypes;
import net.evarius.tnadditions.marking.RoadMarking;
import net.evarius.tnadditions.marking.network.MarkingPayloads;
import net.evarius.tnadditions.marking.network.RoadMarkingNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public final class RoadMarkingEditorSession {
    public enum EditMode { SELECT, ADD, MOVE, INSERT }
    public enum WorkflowMode { PLACE, EDIT }

    private static UUID id;
    private static Identifier type = MarkingTypes.SOLID;
    private static MarkingStyle style = MarkingStyle.DEFAULT;
    private static final List<Vec3d> points = new ArrayList<>();
    private static EditMode mode = EditMode.ADD;
    private static WorkflowMode workflowMode = WorkflowMode.PLACE;
    private static int selectedPoint = -1;
    private static long revision;
    private static boolean persistedSelection;
    private static boolean dirty;

    public static void newDraft() {
        id = UUID.randomUUID();
        points.clear();
        revision = 0;
        selectedPoint = -1;
        mode = EditMode.ADD;
        workflowMode = WorkflowMode.PLACE;
        persistedSelection = false;
        dirty = false;
    }

    public static boolean active() {
        return id != null;
    }

    public static RoadMarking preview() {
        if (id == null || points.size() < 2) return null;
        return new RoadMarking(id, type, points, style, revision);
    }

    public static void handlePoint(Vec3d point) {
        if (!active()) newDraft();
        if (mode == EditMode.SELECT) return;
        point = MarkingPlacementSettings.apply(point, snapReferences());
        switch (mode) {
            case SELECT -> {
                return;
            }
            case ADD -> {
                points.add(point);
                selectedPoint = points.size() - 1;
            }
            case INSERT -> {
                int index = selectedPoint < 0 ? points.size() : Math.min(points.size(), selectedPoint + 1);
                points.add(index, point);
                selectedPoint = index;
                mode = EditMode.ADD;
            }
            case MOVE -> {
                if (selectedPoint >= 0 && selectedPoint < points.size()) points.set(selectedPoint, point);
                mode = EditMode.ADD;
            }
        }
        revision++;
        dirty = true;
        ClientRoadMarkings.GEOMETRY_CACHE.invalidate(id);
    }

    public static boolean handleWorldClick(Vec3d point) {
        if (workflowMode == WorkflowMode.PLACE) {
            if (!active()) newDraft();
            mode = EditMode.ADD;
            handlePoint(point);
            return true;
        }
        if (mode == EditMode.SELECT || !active()) {
            return selectNearest(point, 2.0);
        }
        handlePoint(point);
        return true;
    }

    public static Vec3d previewPlacementPoint(Vec3d raw) {
        if (workflowMode == WorkflowMode.EDIT && mode == EditMode.SELECT) return raw;
        return MarkingPlacementSettings.apply(raw, snapReferences());
    }

    public static RoadMarking previewWithPoint(Vec3d raw) {
        if (workflowMode == WorkflowMode.EDIT && mode == EditMode.SELECT) return preview();
        Vec3d point = MarkingPlacementSettings.apply(raw, snapReferences());
        List<Vec3d> previewPoints = new ArrayList<>(points);
        switch (mode) {
            case ADD -> previewPoints.add(point);
            case INSERT -> {
                int index = selectedPoint < 0 ? previewPoints.size()
                        : Math.min(previewPoints.size(), selectedPoint + 1);
                previewPoints.add(index, point);
            }
            case MOVE -> {
                if (selectedPoint >= 0 && selectedPoint < previewPoints.size()) {
                    previewPoints.set(selectedPoint, point);
                }
            }
            case SELECT -> {
            }
        }
        if (previewPoints.size() < 2) return null;
        UUID previewId = id == null ? UUID.nameUUIDFromBytes("tnadditions-preview".getBytes()) : id;
        return new RoadMarking(previewId, type, previewPoints, style, revision + 1);
    }

    public static boolean selectNearest(Vec3d position, double radius) {
        RoadMarking nearest = ClientRoadMarkings.all().stream()
                .filter(marking -> distanceToPath(marking.controlPoints(), position) <= radius * radius)
                .min(Comparator.comparingDouble(marking -> distanceToPath(marking.controlPoints(), position)))
                .orElse(null);
        if (nearest == null) return false;
        load(nearest);
        selectNearestPoint(position);
        return true;
    }

    public static void load(RoadMarking marking) {
        id = marking.id();
        type = marking.type();
        style = marking.style();
        points.clear();
        points.addAll(marking.controlPoints());
        revision = marking.revision();
        selectedPoint = -1;
        mode = EditMode.SELECT;
        workflowMode = WorkflowMode.EDIT;
        persistedSelection = true;
        dirty = false;
    }

    public static void selectNearestPoint(Vec3d position) {
        selectedPoint = -1;
        double best = Double.MAX_VALUE;
        for (int i = 0; i < points.size(); i++) {
            double distance = points.get(i).squaredDistanceTo(position);
            if (distance < best) {
                best = distance;
                selectedPoint = i;
            }
        }
    }

    public static boolean selectActivePoint(Vec3d position, double radius) {
        if (!active() || points.isEmpty()) return false;
        selectNearestPoint(position);
        return selectedPoint >= 0 && points.get(selectedPoint).squaredDistanceTo(position) <= radius * radius;
    }

    public static boolean removeSelectedPoint() {
        if (selectedPoint < 0 || selectedPoint >= points.size()) return false;
        if (points.size() <= 2) return false;
        points.remove(selectedPoint);
        selectedPoint = Math.min(selectedPoint, points.size() - 1);
        revision++;
        dirty = true;
        ClientRoadMarkings.GEOMETRY_CACHE.invalidate(id);
        return true;
    }

    public static boolean removeLastPoint() {
        if (workflowMode != WorkflowMode.PLACE || points.isEmpty()) return false;
        points.removeLast();
        selectedPoint = points.size() - 1;
        revision++;
        dirty = !points.isEmpty();
        if (points.isEmpty()) clear();
        else ClientRoadMarkings.GEOMETRY_CACHE.invalidate(id);
        return true;
    }

    public static boolean save() {
        RoadMarking marking = preview();
        if (marking == null || !ClientPlayNetworking.canSend(MarkingPayloads.Upsert.ID)) return false;
        ClientPlayNetworking.send(new MarkingPayloads.Upsert(marking.toNbt()));
        clear();
        return true;
    }

    public static boolean delete() {
        if (id == null || !persistedSelection || !ClientPlayNetworking.canSend(MarkingPayloads.Delete.ID)) return false;
        ClientPlayNetworking.send(new MarkingPayloads.Delete(RoadMarkingNetworking.deleteData(id)));
        onDeleted(id);
        return true;
    }

    public static void onDeleted(UUID deletedId) {
        if (deletedId.equals(id)) {
            clear();
        }
    }

    public static void clear() {
        id = null;
        points.clear();
        selectedPoint = -1;
        mode = workflowMode == WorkflowMode.PLACE ? EditMode.ADD : EditMode.SELECT;
        persistedSelection = false;
        dirty = false;
    }

    public static List<Vec3d> points() { return List.copyOf(points); }
    public static int selectedPoint() { return selectedPoint; }
    public static Identifier type() { return type; }
    public static MarkingStyle style() { return style; }
    public static EditMode mode() { return mode; }
    public static void setMode(EditMode value) { mode = value; }
    public static WorkflowMode workflowMode() { return workflowMode; }
    public static boolean persistedSelection() { return persistedSelection; }
    public static boolean dirty() { return dirty; }
    public static boolean setWorkflowMode(WorkflowMode value) {
        if (value == workflowMode) return true;
        if (active() && dirty) return false;
        clear();
        workflowMode = value;
        mode = value == WorkflowMode.PLACE ? EditMode.ADD : EditMode.SELECT;
        return true;
    }
    public static void setType(Identifier value) {
        type = value;
        if (active()) {
            revision++;
            dirty = true;
            invalidate();
        }
    }
    public static void setStyle(MarkingStyle value) {
        style = value.normalized();
        if (active()) {
            revision++;
            dirty = true;
            invalidate();
        }
    }

    public static Text statusText() {
        return Text.translatable("screen.terranexus.road_marking_editor.status",
                points.size(), workflowMode.name().toLowerCase(), mode.name().toLowerCase());
    }

    private static void invalidate() {
        if (id != null) ClientRoadMarkings.GEOMETRY_CACHE.invalidate(id);
    }

    private static List<Vec3d> snapReferences() {
        return switch (mode) {
            case ADD -> points;
            case INSERT -> selectedPoint < 0 ? points
                    : points.subList(0, Math.min(points.size(), selectedPoint + 1));
            case MOVE -> selectedPoint <= 0 ? List.of()
                    : points.subList(0, Math.min(points.size(), selectedPoint));
            case SELECT -> List.of();
        };
    }

    private static double distanceToPath(List<Vec3d> path, Vec3d point) {
        double best = Double.MAX_VALUE;
        for (int i = 1; i < path.size(); i++) {
            Vec3d a = path.get(i - 1);
            Vec3d delta = path.get(i).subtract(a);
            double denominator = delta.lengthSquared();
            double factor = denominator < 1.0E-8 ? 0.0
                    : Math.clamp(point.subtract(a).dotProduct(delta) / denominator, 0.0, 1.0);
            best = Math.min(best, a.add(delta.multiply(factor)).squaredDistanceTo(point));
        }
        return best;
    }

    private RoadMarkingEditorSession() {
    }
}

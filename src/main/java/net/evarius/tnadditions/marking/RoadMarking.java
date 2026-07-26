package net.evarius.tnadditions.marking;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record RoadMarking(
        UUID id,
        Identifier type,
        List<Vec3d> controlPoints,
        MarkingStyle style,
        long revision,
        boolean enabled
) {
    public RoadMarking(UUID id, Identifier type, List<Vec3d> controlPoints, MarkingStyle style, long revision) {
        this(id, type, controlPoints, style, revision, true);
    }

    public RoadMarking {
        controlPoints = List.copyOf(controlPoints);
        if (controlPoints.size() < 2) {
            throw new IllegalArgumentException("A road marking needs at least two control points");
        }
        style = style.normalized();
    }

    public RoadMarking withPoints(List<Vec3d> points) {
        return new RoadMarking(id, type, points, style, revision + 1, enabled);
    }

    public RoadMarking withStyle(MarkingStyle newStyle) {
        return new RoadMarking(id, type, controlPoints, newStyle, revision + 1, enabled);
    }

    public RoadMarking withEnabled(boolean value) {
        return new RoadMarking(id, type, controlPoints, style, revision + 1, value);
    }

    public Box bounds() {
        Vec3d first = controlPoints.getFirst();
        double margin = Math.max(1.0, style.width() * 2.0 + Math.abs(style.lateralOffset()));
        return new Box(
                controlPoints.stream().mapToDouble(p -> p.x).min().orElse(first.x) - margin,
                controlPoints.stream().mapToDouble(p -> p.y).min().orElse(first.y) - margin,
                controlPoints.stream().mapToDouble(p -> p.z).min().orElse(first.z) - margin,
                controlPoints.stream().mapToDouble(p -> p.x).max().orElse(first.x) + margin,
                controlPoints.stream().mapToDouble(p -> p.y).max().orElse(first.y) + margin,
                controlPoints.stream().mapToDouble(p -> p.z).max().orElse(first.z) + margin
        );
    }

    public NbtCompound toNbt() {
        NbtCompound nbt = new NbtCompound();
        nbt.putString("id", id.toString());
        nbt.putString("type", type.toString());
        nbt.putLong("revision", revision);
        nbt.putBoolean("enabled", enabled);
        nbt.put("style", style.toNbt());
        NbtList points = new NbtList();
        for (Vec3d point : controlPoints) {
            NbtCompound entry = new NbtCompound();
            entry.putDouble("x", point.x);
            entry.putDouble("y", point.y);
            entry.putDouble("z", point.z);
            points.add(entry);
        }
        nbt.put("points", points);
        return nbt;
    }

    public static RoadMarking fromNbt(NbtCompound nbt) {
        List<Vec3d> points = new ArrayList<>();
        for (var element : nbt.getListOrEmpty("points")) {
            element.asCompound().ifPresent(point -> points.add(new Vec3d(
                    point.getDouble("x", 0.0),
                    point.getDouble("y", 0.0),
                    point.getDouble("z", 0.0)
            )));
        }
        return new RoadMarking(
                UUID.fromString(nbt.getString("id", UUID.randomUUID().toString())),
                Identifier.of(nbt.getString("type", MarkingTypes.SOLID.toString())),
                points,
                MarkingStyle.fromNbt(nbt.getCompoundOrEmpty("style")),
                nbt.getLong("revision", 0L),
                nbt.getBoolean("enabled", true)
        );
    }
}

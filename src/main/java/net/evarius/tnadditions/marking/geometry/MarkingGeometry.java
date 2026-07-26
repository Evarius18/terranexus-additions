package net.evarius.tnadditions.marking.geometry;

import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public record MarkingGeometry(List<Quad> quads, Box bounds, double length) {
    public record Quad(Vec3d a, Vec3d b, Vec3d c, Vec3d d) {
    }
}

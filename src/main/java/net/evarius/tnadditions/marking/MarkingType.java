package net.evarius.tnadditions.marking;

import net.evarius.tnadditions.marking.geometry.MarkingGeometry;
import net.evarius.tnadditions.marking.spline.SplineSample;
import net.minecraft.util.Identifier;

import java.util.List;

public interface MarkingType {
    Identifier id();

    MarkingGeometry generate(List<SplineSample> samples, MarkingStyle style);
}

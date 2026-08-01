package net.evarius.tnadditions.traffic;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

public record TrafficProgram(String id, String name, List<TrafficPhase> phases) {
    public static final Codec<TrafficProgram> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("id").forGetter(TrafficProgram::id),
            Codec.STRING.fieldOf("name").forGetter(TrafficProgram::name),
            TrafficPhase.CODEC.listOf().fieldOf("phases").forGetter(TrafficProgram::phases)
    ).apply(instance, TrafficProgram::new));

    public TrafficProgram {
        id = normalize(id, "program");
        name = name == null || name.isBlank() ? id : name.trim();
        phases = phases == null || phases.isEmpty() ? defaultPhases() : List.copyOf(phases.subList(0, Math.min(32, phases.size())));
    }

    public int durationTicks() { return phases.stream().mapToInt(TrafficPhase::durationTicks).sum(); }
    public static List<TrafficPhase> defaultPhases() {
        return List.of(new TrafficPhase("red", 200), new TrafficPhase("red_yellow", 40),
                new TrafficPhase("green", 240), new TrafficPhase("yellow", 60));
    }
    public static String normalize(String value, String fallback) {
        String normalized = value == null ? "" : value.trim().toLowerCase(java.util.Locale.ROOT)
                .replaceAll("[^a-z0-9_-]", "_");
        if (normalized.isBlank()) normalized = fallback;
        return normalized.substring(0, Math.min(48, normalized.length()));
    }
}

package net.evarius.tnadditions.traffic;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/** Reusable, persistent content preset for digital traffic displays. */
public record TrafficDisplayTemplate(String id, String mode, int value, String text, boolean enabled) {
    public static final Codec<TrafficDisplayTemplate> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("id").forGetter(TrafficDisplayTemplate::id),
            Codec.STRING.fieldOf("mode").forGetter(TrafficDisplayTemplate::mode),
            Codec.INT.optionalFieldOf("value", 0).forGetter(TrafficDisplayTemplate::value),
            Codec.STRING.optionalFieldOf("text", "").forGetter(TrafficDisplayTemplate::text),
            Codec.BOOL.optionalFieldOf("enabled", true).forGetter(TrafficDisplayTemplate::enabled)
    ).apply(instance, TrafficDisplayTemplate::new));
    public TrafficDisplayTemplate {
        id = TrafficProgram.normalize(id, "template");
        mode = TrafficDisplayMode.parse(mode).asString();
        value = Math.max(0, Math.min(999, value));
        text = text == null ? "" : text.trim().substring(0, Math.min(80, text.trim().length()));
    }
}

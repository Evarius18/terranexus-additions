package net.evarius.tnadditions.traffic;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record TrafficPhase(String aspect, int durationTicks) {
    public static final Codec<TrafficPhase> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("aspect").forGetter(TrafficPhase::aspect),
            Codec.INT.fieldOf("duration_ticks").forGetter(TrafficPhase::durationTicks)
    ).apply(instance, TrafficPhase::new));

    public TrafficPhase {
        aspect = TrafficSignalAspect.parse(aspect).asString();
        durationTicks = Math.max(10, Math.min(durationTicks, 72_000));
    }
}

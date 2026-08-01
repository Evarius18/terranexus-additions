package net.evarius.tnadditions.traffic;

import net.minecraft.util.StringIdentifiable;

public enum TrafficDisplayMode implements StringIdentifiable {
    OFF("off"), SPEED("speed"), CONSTRUCTION("construction"), LANE_CLOSED("lane_closed"),
    DANGER("danger"), TEXT("text");

    private final String id;
    TrafficDisplayMode(String id) { this.id = id; }
    @Override public String asString() { return id; }
    public TrafficDisplayMode next() { return values()[(ordinal() + 1) % values().length]; }
    public static TrafficDisplayMode parse(String value) {
        for (TrafficDisplayMode mode : values()) if (mode.id.equalsIgnoreCase(value)) return mode;
        return OFF;
    }
}

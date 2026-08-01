package net.evarius.tnadditions.traffic;

import net.minecraft.util.StringIdentifiable;

public enum TrafficSignalAspect implements StringIdentifiable {
    OFF("off"), RED("red"), RED_YELLOW("red_yellow"), GREEN("green"), YELLOW("yellow"), FLASHING_YELLOW("flashing_yellow");
    private final String id;
    TrafficSignalAspect(String id) { this.id = id; }
    @Override public String asString() { return id; }
    public TrafficSignalAspect nextManual() { return values()[(ordinal() + 1) % values().length]; }
    public static TrafficSignalAspect parse(String value) {
        for (TrafficSignalAspect aspect : values()) if (aspect.id.equalsIgnoreCase(value)) return aspect;
        return RED;
    }
}

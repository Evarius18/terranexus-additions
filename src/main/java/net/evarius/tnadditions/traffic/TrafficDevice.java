package net.evarius.tnadditions.traffic;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.math.BlockPos;

public record TrafficDevice(String dimension, long position, String type, String groupId, String areaId,
                            String displayMode, int displayValue, String displayText, boolean enabled,
                            String programId, boolean manual, String manualAspect, String name,
                            String intersectionId, int scheduleStart, int scheduleEnd) {
    public static final Codec<TrafficDevice> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("dimension").forGetter(TrafficDevice::dimension),
            Codec.LONG.fieldOf("position").forGetter(TrafficDevice::position),
            Codec.STRING.fieldOf("type").forGetter(TrafficDevice::type),
            Codec.STRING.optionalFieldOf("group", "default").forGetter(TrafficDevice::groupId),
            Codec.STRING.optionalFieldOf("area", "").forGetter(TrafficDevice::areaId),
            Codec.STRING.optionalFieldOf("display_mode", "off").forGetter(TrafficDevice::displayMode),
            Codec.INT.optionalFieldOf("display_value", 0).forGetter(TrafficDevice::displayValue),
            Codec.STRING.optionalFieldOf("display_text", "").forGetter(TrafficDevice::displayText),
            Codec.BOOL.optionalFieldOf("enabled", true).forGetter(TrafficDevice::enabled),
            Codec.STRING.optionalFieldOf("program", "default").forGetter(TrafficDevice::programId),
            Codec.BOOL.optionalFieldOf("manual", false).forGetter(TrafficDevice::manual),
            Codec.STRING.optionalFieldOf("manual_aspect", "red").forGetter(TrafficDevice::manualAspect),
            Codec.STRING.optionalFieldOf("name", "").forGetter(TrafficDevice::name),
            Codec.STRING.optionalFieldOf("intersection", "").forGetter(TrafficDevice::intersectionId),
            Codec.INT.optionalFieldOf("schedule_start", -1).forGetter(TrafficDevice::scheduleStart),
            Codec.INT.optionalFieldOf("schedule_end", -1).forGetter(TrafficDevice::scheduleEnd)
    ).apply(instance, TrafficDevice::new));

    public TrafficDevice {
        groupId = TrafficProgram.normalize(groupId, "default");
        areaId = areaId == null ? "" : areaId.trim();
        displayMode = TrafficDisplayMode.parse(displayMode).asString();
        displayValue = Math.max(0, Math.min(999, displayValue));
        displayText = displayText == null ? "" : displayText.trim().substring(0, Math.min(80, displayText.trim().length()));
        programId = TrafficProgram.normalize(programId, "default");
        manualAspect = TrafficSignalAspect.parse(manualAspect).asString();
        name = clean(name, 64);
        intersectionId = TrafficProgram.normalize(intersectionId, "");
        scheduleStart = clampSchedule(scheduleStart);
        scheduleEnd = clampSchedule(scheduleEnd);
    }
    public BlockPos blockPos() { return BlockPos.fromLong(position); }
    public String key() { return key(dimension, position); }
    public static String key(String dimension, long position) { return dimension + "@" + position; }
    public TrafficDevice withDisplay(TrafficDisplayMode mode, int value, String text, boolean enabled) {
        return new TrafficDevice(dimension, position, type, groupId, areaId, mode.asString(), value, text, enabled,
                programId, manual, manualAspect, name, intersectionId, scheduleStart, scheduleEnd);
    }
    public TrafficDevice withRouting(String group, String area) {
        return new TrafficDevice(dimension, position, type, group, area, displayMode, displayValue, displayText, enabled,
                programId, manual, manualAspect, name, intersectionId, scheduleStart, scheduleEnd);
    }
    public TrafficDevice withProgram(String program, boolean manual, TrafficSignalAspect aspect) {
        return new TrafficDevice(dimension, position, type, groupId, areaId, displayMode, displayValue, displayText, enabled,
                program, manual, aspect.asString(), name, intersectionId, scheduleStart, scheduleEnd);
    }
    public TrafficDevice withIdentity(String newName, String intersection) {
        return new TrafficDevice(dimension, position, type, groupId, areaId, displayMode, displayValue, displayText, enabled,
                programId, manual, manualAspect, newName, intersection, scheduleStart, scheduleEnd);
    }
    public TrafficDevice withSchedule(int start, int end) {
        return new TrafficDevice(dimension, position, type, groupId, areaId, displayMode, displayValue, displayText, enabled,
                programId, manual, manualAspect, name, intersectionId, start, end);
    }
    public boolean scheduledActive(long worldTime) {
        if (scheduleStart < 0 || scheduleEnd < 0 || scheduleStart == scheduleEnd) return true;
        int time = Math.floorMod((int) worldTime, 24_000);
        return scheduleStart < scheduleEnd ? time >= scheduleStart && time < scheduleEnd
                : time >= scheduleStart || time < scheduleEnd;
    }
    public String displayName() { return name.isBlank() ? type + " " + blockPos().toShortString() : name; }
    private static int clampSchedule(int value) { return value < 0 ? -1 : Math.min(23_999, value); }
    private static String clean(String value, int max) {
        String clean = value == null ? "" : value.trim();
        return clean.substring(0, Math.min(max, clean.length()));
    }
}

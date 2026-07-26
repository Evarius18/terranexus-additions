package net.evarius.tnadditions.marking;

import net.evarius.tnadditions.marking.geometry.RibbonGeometryGenerator;
import net.minecraft.util.Identifier;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Open registry for marking strategies. Add-ons may register a new strategy
 * without modifying the editor, spline sampler, storage or renderer.
 */
public final class MarkingTypes {
    private static final Map<Identifier, MarkingType> TYPES = new LinkedHashMap<>();
    private static final String NS = "tnadditions";

    public static final Identifier SOLID = id("solid");
    public static final Identifier DASHED = id("dashed");
    public static final Identifier DOUBLE = id("double");
    public static final Identifier DOUBLE_DASHED = id("double_dashed");
    public static final Identifier GUIDE = id("guide_line");
    public static final Identifier LANE_DIVIDER = id("lane_divider");
    public static final Identifier STOP = id("stop_line");
    public static final Identifier HATCH = id("hatched_area");
    public static final Identifier PARKING = id("parking");
    public static final Identifier CROSSWALK = id("crosswalk");
    public static final Identifier BIKE = id("bike_lane");
    public static final Identifier BUS = id("bus_lane");
    public static final Identifier ARROW = id("direction_arrow");
    public static final Identifier TURN_ARROW = id("turn_arrow");

    public static void registerDefaults() {
        register(new RibbonGeometryGenerator(SOLID, RibbonGeometryGenerator.Pattern.SOLID));
        register(new RibbonGeometryGenerator(DASHED, RibbonGeometryGenerator.Pattern.DASHED));
        register(new RibbonGeometryGenerator(DOUBLE, RibbonGeometryGenerator.Pattern.DOUBLE));
        register(new RibbonGeometryGenerator(DOUBLE_DASHED, RibbonGeometryGenerator.Pattern.DOUBLE_DASHED));
        register(new RibbonGeometryGenerator(GUIDE, RibbonGeometryGenerator.Pattern.DASHED));
        register(new RibbonGeometryGenerator(LANE_DIVIDER, RibbonGeometryGenerator.Pattern.DASHED));
        register(new RibbonGeometryGenerator(STOP, RibbonGeometryGenerator.Pattern.STOP));
        register(new RibbonGeometryGenerator(HATCH, RibbonGeometryGenerator.Pattern.HATCH));
        register(new RibbonGeometryGenerator(PARKING, RibbonGeometryGenerator.Pattern.PARKING));
        register(new RibbonGeometryGenerator(CROSSWALK, RibbonGeometryGenerator.Pattern.CROSSWALK));
        register(new RibbonGeometryGenerator(BIKE, RibbonGeometryGenerator.Pattern.SYMBOL));
        register(new RibbonGeometryGenerator(BUS, RibbonGeometryGenerator.Pattern.SYMBOL));
        register(new RibbonGeometryGenerator(ARROW, RibbonGeometryGenerator.Pattern.ARROW));
        register(new RibbonGeometryGenerator(TURN_ARROW, RibbonGeometryGenerator.Pattern.TURN_ARROW));
    }

    public static void register(MarkingType type) {
        TYPES.put(type.id(), type);
    }

    public static MarkingType get(Identifier id) {
        return TYPES.getOrDefault(id, TYPES.get(SOLID));
    }

    public static Collection<MarkingType> values() {
        return TYPES.values();
    }

    private static Identifier id(String path) {
        return Identifier.of(NS, path);
    }

    private MarkingTypes() {
    }
}

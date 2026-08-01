package net.evarius.tnadditions.traffic;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.evarius.tnadditions.config.InfrastructureConfig;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class TrafficControlState extends PersistentState {
    private static final Codec<TrafficControlState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(Codec.STRING, TrafficDevice.CODEC).optionalFieldOf("devices", Map.of()).forGetter(s -> s.devices),
            Codec.unboundedMap(Codec.STRING, TrafficProgram.CODEC).optionalFieldOf("programs", Map.of()).forGetter(s -> s.programs),
            Codec.unboundedMap(Codec.STRING, TrafficDisplayTemplate.CODEC).optionalFieldOf("display_templates", Map.of()).forGetter(s -> s.templates)
    ).apply(instance, TrafficControlState::new));
    private static final PersistentStateType<TrafficControlState> TYPE = new PersistentStateType<>(
            "terranexus_traffic_control", TrafficControlState::new, CODEC, DataFixTypes.LEVEL);

    private final Map<String, TrafficDevice> devices;
    private final Map<String, TrafficProgram> programs;
    private final Map<String, TrafficDisplayTemplate> templates;

    public TrafficControlState() { this(new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>()); }
    private TrafficControlState(Map<String, TrafficDevice> devices, Map<String, TrafficProgram> programs,
                                Map<String, TrafficDisplayTemplate> templates) {
        this.devices = new LinkedHashMap<>(devices);
        this.programs = new LinkedHashMap<>(programs);
        this.templates = new LinkedHashMap<>(templates);
        this.programs.putIfAbsent("default", new TrafficProgram("default", "Standard", TrafficProgram.defaultPhases()));
    }

    public static TrafficControlState get(MinecraftServer server) {
        return server.getOverworld().getPersistentStateManager().getOrCreate(TYPE);
    }

    public TrafficDevice register(ServerWorld world, BlockPos pos, TrafficDeviceType type) {
        String dimension = world.getRegistryKey().getValue().toString();
        String key = TrafficDevice.key(dimension, pos.asLong());
        TrafficDevice existing = devices.get(key);
        if (existing != null) return existing;
        if (devices.size() >= InfrastructureConfig.maximumDevices()) return null;
        TrafficDevice created = new TrafficDevice(dimension, pos.asLong(), type.name().toLowerCase(),
                InfrastructureConfig.defaultDeviceGroup(), "", "off", 0, "", true,
                "default", false, "red", "", "", -1, -1);
        devices.put(key, created);
        markDirty();
        return created;
    }

    public TrafficDevice device(ServerWorld world, BlockPos pos) {
        return devices.get(TrafficDevice.key(world.getRegistryKey().getValue().toString(), pos.asLong()));
    }
    public TrafficDevice device(String key) { return devices.get(key); }
    public Collection<TrafficDevice> devices() { return List.copyOf(devices.values()); }
    public Collection<TrafficProgram> programs() { return List.copyOf(programs.values()); }
    public TrafficProgram program(String id) { return programs.getOrDefault(id, programs.get("default")); }
    public void update(TrafficDevice device) { devices.put(device.key(), device); markDirty(); }
    public void remove(ServerWorld world, BlockPos pos) {
        if (devices.remove(TrafficDevice.key(world.getRegistryKey().getValue().toString(), pos.asLong())) != null) markDirty();
    }
    public boolean putProgram(TrafficProgram program) {
        if (!programs.containsKey(program.id()) && programs.size() >= InfrastructureConfig.maximumPrograms()) return false;
        programs.put(program.id(), program); markDirty(); return true;
    }
    public TrafficDisplayTemplate template(String id) { return templates.get(TrafficProgram.normalize(id, "")); }
    public Collection<TrafficDisplayTemplate> templates() { return List.copyOf(templates.values()); }
    public void putTemplate(TrafficDisplayTemplate template) { templates.put(template.id(), template); markDirty(); }

    public TrafficSignalAspect aspect(TrafficDevice device, long worldTime) {
        if (!device.enabled() || !device.scheduledActive(worldTime)) return TrafficSignalAspect.OFF;
        if (device.manual()) return TrafficSignalAspect.parse(device.manualAspect());
        TrafficProgram program = program(device.programId());
        int total = Math.max(1, program.durationTicks());
        int cursor = Math.floorMod((int) (worldTime % total), total);
        for (TrafficPhase phase : program.phases()) {
            if (cursor < phase.durationTicks()) return TrafficSignalAspect.parse(phase.aspect());
            cursor -= phase.durationTicks();
        }
        return TrafficSignalAspect.RED;
    }

    public void applyGroup(TrafficDevice source) {
        List<TrafficDevice> changed = new ArrayList<>();
        for (TrafficDevice device : devices.values()) {
            if (!device.type().equals(source.type()) || !device.groupId().equals(source.groupId())) continue;
            changed.add(new TrafficDevice(device.dimension(), device.position(), device.type(), source.groupId(), source.areaId(),
                    source.displayMode(), source.displayValue(), source.displayText(), source.enabled(), source.programId(),
                    source.manual(), source.manualAspect(), device.name(), source.intersectionId(),
                    source.scheduleStart(), source.scheduleEnd()));
        }
        changed.forEach(device -> devices.put(device.key(), device));
        if (!changed.isEmpty()) markDirty();
    }
}

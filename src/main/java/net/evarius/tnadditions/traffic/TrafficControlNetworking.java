package net.evarius.tnadditions.traffic;

import net.evarius.tnadditions.block.custom.DigitalTrafficDisplayBlock;
import net.evarius.tnadditions.infrastructure.InfrastructureAccess;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;

public final class TrafficControlNetworking {
    public static void register() {
        PayloadTypeRegistry.playS2C().register(TrafficControlPayloads.Open.ID, TrafficControlPayloads.Open.CODEC);
        PayloadTypeRegistry.playC2S().register(TrafficControlPayloads.Action.ID, TrafficControlPayloads.Action.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(TrafficControlPayloads.Action.ID, (payload, context) ->
                context.server().execute(() -> apply(context.player(), payload.data())));
    }

    public static void open(ServerPlayerEntity player) {
        if (!InfrastructureAccess.mayConfigure(player)) return;
        NbtCompound root = new NbtCompound();
        NbtList devices = new NbtList();
        for (TrafficDevice device : TrafficControlState.get(player.getServer()).devices()) {
            NbtCompound entry = new NbtCompound();
            entry.putString("key", device.key());
            entry.putString("name", device.displayName());
            entry.putString("type", device.type());
            entry.putString("group", device.groupId());
            entry.putString("area", device.areaId());
            entry.putString("intersection", device.intersectionId());
            entry.putString("mode", device.displayMode());
            entry.putInt("value", device.displayValue());
            entry.putString("text", device.displayText());
            entry.putBoolean("enabled", device.enabled());
            entry.putString("program", device.programId());
            entry.putBoolean("manual", device.manual());
            entry.putString("aspect", device.manualAspect());
            entry.putInt("schedule_start", device.scheduleStart());
            entry.putInt("schedule_end", device.scheduleEnd());
            entry.putString("position", device.blockPos().toShortString());
            devices.add(entry);
        }
        root.put("devices", devices);
        ServerPlayNetworking.send(player, new TrafficControlPayloads.Open(root));
    }

    private static void apply(ServerPlayerEntity player, NbtCompound data) {
        if (!InfrastructureAccess.mayConfigure(player)) return;
        TrafficControlState control = TrafficControlState.get(player.getServer());
        TrafficDevice device = control.device(data.getString("key", ""));
        if (device == null) return;
        String action = data.getString("action", "");
        TrafficDevice updated = switch (action) {
            case "toggle_enabled" -> device.withDisplay(TrafficDisplayMode.parse(device.displayMode()),
                    device.displayValue(), device.displayText(), !device.enabled());
            case "cycle_mode" -> device.withDisplay(TrafficDisplayMode.parse(device.displayMode()).next(),
                    device.displayValue(), device.displayText(), device.enabled());
            case "cycle_aspect" -> device.withProgram(device.programId(), true,
                    TrafficSignalAspect.parse(device.manualAspect()).nextManual());
            case "toggle_automatic" -> device.withProgram(device.programId(), !device.manual(),
                    TrafficSignalAspect.parse(device.manualAspect()));
            case "configure" -> device.withIdentity(data.getString("name", ""),
                            data.getString("intersection", ""))
                    .withRouting(data.getString("group", "default"), data.getString("area", ""))
                    .withDisplay(TrafficDisplayMode.parse(device.displayMode()), data.getInt("value", 0),
                            data.getString("text", ""), device.enabled())
                    .withSchedule(data.getInt("schedule_start", -1), data.getInt("schedule_end", -1));
            default -> device;
        };
        control.update(updated);
        if ("apply_group".equals(action)) control.applyGroup(updated);
        sync(player, updated);
        open(player);
    }

    private static void sync(ServerPlayerEntity player, TrafficDevice device) {
        ServerWorld world = player.getServer().getWorld(RegistryKey.of(RegistryKeys.WORLD,
                Identifier.of(device.dimension())));
        if (world == null || !world.isChunkLoaded(device.blockPos())) return;
        var state = world.getBlockState(device.blockPos());
        if (state.contains(DigitalTrafficDisplayBlock.MODE)) {
            TrafficDisplayMode mode = device.enabled() && device.scheduledActive(world.getTime())
                    ? TrafficDisplayMode.parse(device.displayMode()) : TrafficDisplayMode.OFF;
            world.setBlockState(device.blockPos(), state.with(DigitalTrafficDisplayBlock.MODE, mode), Block.NOTIFY_LISTENERS);
        }
        world.scheduleBlockTick(device.blockPos(), state.getBlock(), 1);
    }
    private TrafficControlNetworking() {}
}

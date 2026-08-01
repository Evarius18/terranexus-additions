package net.evarius.tnadditions.garage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateType;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class GarageAccessState extends PersistentState {
    private static final Codec<GarageAccessState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(Codec.STRING, GarageDoorAccess.CODEC).optionalFieldOf("doors", Map.of()).forGetter(s -> s.doors)
    ).apply(instance, GarageAccessState::new));
    private static final PersistentStateType<GarageAccessState> TYPE = new PersistentStateType<>(
            "tnadditions_garage_access", GarageAccessState::new, CODEC, DataFixTypes.LEVEL);
    private final Map<String, GarageDoorAccess> doors;
    public GarageAccessState() { this(new LinkedHashMap<>()); }
    private GarageAccessState(Map<String, GarageDoorAccess> doors) { this.doors = new LinkedHashMap<>(doors); }
    public static GarageAccessState get(MinecraftServer server) { return server.getOverworld().getPersistentStateManager().getOrCreate(TYPE); }
    public GarageDoorAccess register(ServerWorld world, BlockPos pos, UUID owner) {
        String dimension = world.getRegistryKey().getValue().toString();
        String key = dimension + "@" + pos.asLong();
        GarageDoorAccess access = doors.get(key);
        if (access == null) { access = new GarageDoorAccess(dimension, pos.asLong(), owner.toString(), List.of()); doors.put(key, access); markDirty(); }
        return access;
    }
    public GarageDoorAccess get(ServerWorld world, BlockPos pos) { return doors.get(key(world, pos)); }
    public boolean permits(ServerPlayerEntity player, ServerWorld world, BlockPos pos) {
        GarageDoorAccess access = get(world, pos);
        return access == null || access.permits(player.getUuid()) || player.hasPermissionLevel(2);
    }
    public boolean mayManage(ServerPlayerEntity player, ServerWorld world, BlockPos pos) {
        GarageDoorAccess access = get(world, pos);
        return access == null || access.owner().equals(player.getUuidAsString()) || player.hasPermissionLevel(2);
    }
    public boolean grant(ServerWorld world, BlockPos pos, UUID player) {
        GarageDoorAccess access = get(world, pos); if (access == null) return false;
        List<String> authorized = new ArrayList<>(access.authorized());
        if (!authorized.contains(player.toString())) authorized.add(player.toString());
        doors.put(access.key(), access.withAuthorized(authorized)); markDirty(); return true;
    }
    public boolean revoke(ServerWorld world, BlockPos pos, UUID player) {
        GarageDoorAccess access = get(world, pos); if (access == null) return false;
        List<String> authorized = new ArrayList<>(access.authorized()); authorized.remove(player.toString());
        doors.put(access.key(), access.withAuthorized(authorized)); markDirty(); return true;
    }
    public void remove(ServerWorld world, BlockPos pos) { if (doors.remove(key(world, pos)) != null) markDirty(); }
    private static String key(ServerWorld world, BlockPos pos) { return world.getRegistryKey().getValue() + "@" + pos.asLong(); }
}

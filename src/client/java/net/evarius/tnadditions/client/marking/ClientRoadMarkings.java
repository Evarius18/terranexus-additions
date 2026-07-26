package net.evarius.tnadditions.client.marking;

import net.evarius.tnadditions.marking.RoadMarking;
import net.evarius.tnadditions.marking.geometry.MarkingGeometryCache;
import net.evarius.tnadditions.marking.network.MarkingPayloads;
import net.evarius.tnadditions.marking.storage.RoadMarkingState;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.nbt.NbtCompound;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public final class ClientRoadMarkings {
    private static final Map<UUID, RoadMarking> MARKINGS = new LinkedHashMap<>();
    public static final MarkingGeometryCache GEOMETRY_CACHE = new MarkingGeometryCache();
    public static final MarkingSpatialIndex SPATIAL_INDEX = new MarkingSpatialIndex();

    public static void registerNetworking() {
        ClientPlayNetworking.registerGlobalReceiver(MarkingPayloads.Snapshot.ID, (payload, context) ->
                context.client().execute(() -> replaceSnapshot(payload.data())));
        ClientPlayNetworking.registerGlobalReceiver(MarkingPayloads.Upsert.ID, (payload, context) ->
                context.client().execute(() -> put(RoadMarking.fromNbt(payload.data()))));
        ClientPlayNetworking.registerGlobalReceiver(MarkingPayloads.Delete.ID, (payload, context) ->
                context.client().execute(() -> remove(payload.data())));
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> clear());
    }

    public static Collection<RoadMarking> all() {
        return java.util.List.copyOf(MARKINGS.values());
    }

    public static Collection<RoadMarking> around(net.minecraft.util.math.Vec3d position, int radiusChunks) {
        return SPATIAL_INDEX.around(position, radiusChunks).stream()
                .map(MARKINGS::get).filter(java.util.Objects::nonNull).toList();
    }

    public static void put(RoadMarking marking) {
        MARKINGS.put(marking.id(), marking);
        SPATIAL_INDEX.put(marking);
        GEOMETRY_CACHE.invalidate(marking.id());
    }

    private static void replaceSnapshot(NbtCompound data) {
        MARKINGS.clear();
        SPATIAL_INDEX.clear();
        for (RoadMarking marking : RoadMarkingState.fromNbt(data).all()) {
            MARKINGS.put(marking.id(), marking);
            SPATIAL_INDEX.put(marking);
        }
        GEOMETRY_CACHE.retain(MARKINGS.keySet());
    }

    private static void remove(NbtCompound data) {
        try {
            UUID id = UUID.fromString(data.getString("id", ""));
            MARKINGS.remove(id);
            SPATIAL_INDEX.remove(id);
            GEOMETRY_CACHE.invalidate(id);
            RoadMarkingEditorSession.onDeleted(id);
        } catch (IllegalArgumentException ignored) {
        }
    }

    private static void clear() {
        MARKINGS.clear();
        SPATIAL_INDEX.clear();
        GEOMETRY_CACHE.retain(java.util.List.of());
        RoadMarkingEditorSession.clear();
    }

    private ClientRoadMarkings() {
    }
}

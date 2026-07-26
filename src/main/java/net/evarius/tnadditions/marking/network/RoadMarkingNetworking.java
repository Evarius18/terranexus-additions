package net.evarius.tnadditions.marking.network;

import net.evarius.tnadditions.marking.RoadMarking;
import net.evarius.tnadditions.marking.storage.RoadMarkingState;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.UUID;

public final class RoadMarkingNetworking {
    private static final double MAX_EDIT_DISTANCE_SQUARED = 256.0 * 256.0;

    public static void register() {
        PayloadTypeRegistry.playS2C().register(MarkingPayloads.Snapshot.ID, MarkingPayloads.Snapshot.CODEC);
        PayloadTypeRegistry.playS2C().register(MarkingPayloads.Upsert.ID, MarkingPayloads.Upsert.CODEC);
        PayloadTypeRegistry.playS2C().register(MarkingPayloads.Delete.ID, MarkingPayloads.Delete.CODEC);
        PayloadTypeRegistry.playC2S().register(MarkingPayloads.Upsert.ID, MarkingPayloads.Upsert.CODEC);
        PayloadTypeRegistry.playC2S().register(MarkingPayloads.Delete.ID, MarkingPayloads.Delete.CODEC);

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> sendSnapshot(handler.player));
        ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register((player, origin, destination) ->
                sendSnapshot(player));
        ServerPlayNetworking.registerGlobalReceiver(MarkingPayloads.Upsert.ID, (payload, context) -> {
            ServerPlayerEntity player = context.player();
            try {
                RoadMarking marking = RoadMarking.fromNbt(payload.data());
                if (!canEdit(player, marking)) return;
                RoadMarkingState state = RoadMarkingState.get(player.getWorld());
                RoadMarking existing = state.get(marking.id());
                if (existing != null && marking.revision() <= existing.revision()) return;
                state.put(marking);
                broadcast(player.getWorld(), new MarkingPayloads.Upsert(marking.toNbt()));
            } catch (RuntimeException ignored) {
                // Invalid or malicious payload.
            }
        });
        ServerPlayNetworking.registerGlobalReceiver(MarkingPayloads.Delete.ID, (payload, context) -> {
            ServerPlayerEntity player = context.player();
            try {
                UUID id = UUID.fromString(payload.data().getString("id", ""));
                RoadMarking current = RoadMarkingState.get(player.getWorld()).get(id);
                if (current == null || !canEdit(player, current)) return;
                RoadMarkingState.get(player.getWorld()).remove(id);
                broadcast(player.getWorld(), payload);
            } catch (RuntimeException ignored) {
                // Invalid or malicious payload.
            }
        });
    }

    public static void sendSnapshot(ServerPlayerEntity player) {
        ServerPlayNetworking.send(player, new MarkingPayloads.Snapshot(
                RoadMarkingState.get(player.getWorld()).toNbt()
        ));
    }

    private static boolean canEdit(ServerPlayerEntity player, RoadMarking marking) {
        if (marking.controlPoints().size() > 512) return false;
        if (marking.controlPoints().stream().anyMatch(point ->
                !Double.isFinite(point.x) || !Double.isFinite(point.y) || !Double.isFinite(point.z))) return false;
        return marking.controlPoints().stream().anyMatch(point ->
                player.squaredDistanceTo(point) <= MAX_EDIT_DISTANCE_SQUARED);
    }

    private static void broadcast(ServerWorld world, net.minecraft.network.packet.CustomPayload payload) {
        for (ServerPlayerEntity player : world.getPlayers()) ServerPlayNetworking.send(player, payload);
    }

    public static void broadcastUpsert(ServerWorld world, RoadMarking marking) {
        broadcast(world, new MarkingPayloads.Upsert(marking.toNbt()));
    }

    public static void broadcastDelete(ServerWorld world, UUID id) {
        broadcast(world, new MarkingPayloads.Delete(deleteData(id)));
    }

    public static NbtCompound deleteData(UUID id) {
        NbtCompound nbt = new NbtCompound();
        nbt.putString("id", id.toString());
        return nbt;
    }

    private RoadMarkingNetworking() {
    }
}

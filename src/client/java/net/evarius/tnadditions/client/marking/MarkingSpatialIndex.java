package net.evarius.tnadditions.client.marking;

import net.evarius.tnadditions.marking.RoadMarking;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Cheap chunk-area lookup used before geometry generation and frustum tests.
 */
public final class MarkingSpatialIndex {
    private final Map<Long, Set<UUID>> chunks = new HashMap<>();
    private final Map<UUID, Set<Long>> membership = new HashMap<>();

    public void put(RoadMarking marking) {
        remove(marking.id());
        Box bounds = marking.bounds();
        int minX = Math.floorDiv((int) Math.floor(bounds.minX), 16);
        int maxX = Math.floorDiv((int) Math.floor(bounds.maxX), 16);
        int minZ = Math.floorDiv((int) Math.floor(bounds.minZ), 16);
        int maxZ = Math.floorDiv((int) Math.floor(bounds.maxZ), 16);
        Set<Long> occupied = new HashSet<>();
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                long key = ChunkPos.toLong(x, z);
                chunks.computeIfAbsent(key, ignored -> new HashSet<>()).add(marking.id());
                occupied.add(key);
            }
        }
        membership.put(marking.id(), occupied);
    }

    public void remove(UUID id) {
        Set<Long> old = membership.remove(id);
        if (old == null) return;
        for (long key : old) {
            Set<UUID> entries = chunks.get(key);
            if (entries == null) continue;
            entries.remove(id);
            if (entries.isEmpty()) chunks.remove(key);
        }
    }

    public List<UUID> around(Vec3d position, int radiusChunks) {
        int centerX = Math.floorDiv((int) Math.floor(position.x), 16);
        int centerZ = Math.floorDiv((int) Math.floor(position.z), 16);
        Set<UUID> found = new HashSet<>();
        for (int x = centerX - radiusChunks; x <= centerX + radiusChunks; x++) {
            for (int z = centerZ - radiusChunks; z <= centerZ + radiusChunks; z++) {
                Set<UUID> entries = chunks.get(ChunkPos.toLong(x, z));
                if (entries != null) found.addAll(entries);
            }
        }
        return new ArrayList<>(found);
    }

    public void clear() {
        chunks.clear();
        membership.clear();
    }
}

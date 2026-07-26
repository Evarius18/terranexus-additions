package net.evarius.tnadditions.marking.storage;

import com.mojang.serialization.Codec;
import net.evarius.tnadditions.marking.RoadMarking;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateType;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Dimension-local parameter storage. Generated vertices never enter the save.
 */
public final class RoadMarkingState extends PersistentState {
    private static final Codec<RoadMarkingState> CODEC =
            NbtCompound.CODEC.xmap(RoadMarkingState::fromNbt, RoadMarkingState::toNbt);
    private static final PersistentStateType<RoadMarkingState> TYPE = new PersistentStateType<>(
            "tnadditions_road_markings",
            RoadMarkingState::new,
            CODEC,
            DataFixTypes.SAVED_DATA_COMMAND_STORAGE
    );

    private final Map<UUID, RoadMarking> markings = new LinkedHashMap<>();

    public static RoadMarkingState get(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(TYPE);
    }

    public Collection<RoadMarking> all() {
        return java.util.List.copyOf(markings.values());
    }

    public RoadMarking get(UUID id) {
        return markings.get(id);
    }

    public void put(RoadMarking marking) {
        markings.put(marking.id(), marking);
        markDirty();
    }

    public boolean remove(UUID id) {
        if (markings.remove(id) == null) return false;
        markDirty();
        return true;
    }

    public NbtCompound toNbt() {
        NbtCompound root = new NbtCompound();
        root.putInt("format", 1);
        NbtList list = new NbtList();
        for (RoadMarking marking : markings.values()) list.add(marking.toNbt());
        root.put("markings", list);
        return root;
    }

    public static RoadMarkingState fromNbt(NbtCompound nbt) {
        RoadMarkingState state = new RoadMarkingState();
        for (var element : nbt.getListOrEmpty("markings")) {
            element.asCompound().ifPresent(entry -> {
                try {
                    RoadMarking marking = RoadMarking.fromNbt(entry);
                    state.markings.put(marking.id(), marking);
                } catch (RuntimeException ignored) {
                    // A malformed entry must not make an otherwise valid world unloadable.
                }
            });
        }
        return state;
    }
}

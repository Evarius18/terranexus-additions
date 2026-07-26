package net.evarius.tnadditions.marking.network;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public final class MarkingPayloads {
    public record Snapshot(NbtCompound data) implements CustomPayload {
        public static final Id<Snapshot> ID = new Id<>(Identifier.of("tnadditions", "marking_snapshot"));
        public static final PacketCodec<RegistryByteBuf, Snapshot> CODEC = nbtCodec(Snapshot::new, Snapshot::data);
        @Override public Id<? extends CustomPayload> getId() { return ID; }
    }

    public record Upsert(NbtCompound data) implements CustomPayload {
        public static final Id<Upsert> ID = new Id<>(Identifier.of("tnadditions", "marking_upsert"));
        public static final PacketCodec<RegistryByteBuf, Upsert> CODEC = nbtCodec(Upsert::new, Upsert::data);
        @Override public Id<? extends CustomPayload> getId() { return ID; }
    }

    public record Delete(NbtCompound data) implements CustomPayload {
        public static final Id<Delete> ID = new Id<>(Identifier.of("tnadditions", "marking_delete"));
        public static final PacketCodec<RegistryByteBuf, Delete> CODEC = nbtCodec(Delete::new, Delete::data);
        @Override public Id<? extends CustomPayload> getId() { return ID; }
    }

    private static <T> PacketCodec<RegistryByteBuf, T> nbtCodec(
            java.util.function.Function<NbtCompound, T> decoder,
            java.util.function.Function<T, NbtCompound> encoder
    ) {
        return PacketCodec.ofStatic(
                (buffer, value) -> buffer.encode(NbtOps.INSTANCE, NbtCompound.CODEC, encoder.apply(value)),
                buffer -> decoder.apply(buffer.decode(NbtOps.INSTANCE, NbtCompound.CODEC))
        );
    }

    private MarkingPayloads() {
    }
}

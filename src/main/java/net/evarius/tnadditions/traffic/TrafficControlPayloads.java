package net.evarius.tnadditions.traffic;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public final class TrafficControlPayloads {
    public record Open(NbtCompound data) implements CustomPayload {
        public static final Id<Open> ID = new Id<>(Identifier.of("tnadditions", "traffic_control_open"));
        public static final PacketCodec<RegistryByteBuf, Open> CODEC = codec(Open::new, Open::data);
        @Override public Id<? extends CustomPayload> getId() { return ID; }
    }
    public record Action(NbtCompound data) implements CustomPayload {
        public static final Id<Action> ID = new Id<>(Identifier.of("tnadditions", "traffic_control_action"));
        public static final PacketCodec<RegistryByteBuf, Action> CODEC = codec(Action::new, Action::data);
        @Override public Id<? extends CustomPayload> getId() { return ID; }
    }
    private static <T> PacketCodec<RegistryByteBuf, T> codec(
            java.util.function.Function<NbtCompound, T> decoder,
            java.util.function.Function<T, NbtCompound> encoder) {
        return PacketCodec.ofStatic(
                (buffer, value) -> buffer.encode(NbtOps.INSTANCE, NbtCompound.CODEC, encoder.apply(value)),
                buffer -> decoder.apply(buffer.decode(NbtOps.INSTANCE, NbtCompound.CODEC)));
    }
    private TrafficControlPayloads() {}
}

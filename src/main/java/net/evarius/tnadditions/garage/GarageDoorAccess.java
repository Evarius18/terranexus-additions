package net.evarius.tnadditions.garage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;
import java.util.UUID;

public record GarageDoorAccess(String dimension, long position, String owner, List<String> authorized) {
    public static final Codec<GarageDoorAccess> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("dimension").forGetter(GarageDoorAccess::dimension),
            Codec.LONG.fieldOf("position").forGetter(GarageDoorAccess::position),
            Codec.STRING.fieldOf("owner").forGetter(GarageDoorAccess::owner),
            Codec.STRING.listOf().optionalFieldOf("authorized", List.of()).forGetter(GarageDoorAccess::authorized)
    ).apply(instance, GarageDoorAccess::new));
    public GarageDoorAccess { authorized = authorized == null ? List.of() : List.copyOf(authorized); }
    public String key() { return dimension + "@" + position; }
    public boolean permits(UUID player) { return owner.equals(player.toString()) || authorized.contains(player.toString()); }
    public GarageDoorAccess withAuthorized(List<String> players) {
        return new GarageDoorAccess(dimension, position, owner, players);
    }
}

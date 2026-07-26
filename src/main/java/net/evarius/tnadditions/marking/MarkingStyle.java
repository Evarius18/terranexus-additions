package net.evarius.tnadditions.marking;

import net.minecraft.nbt.NbtCompound;

/**
 * All visual and behavioral parameters of a road marking. Values are kept in
 * world data instead of baking individual marking segments into the save.
 */
public record MarkingStyle(
        double width,
        int color,
        String material,
        float opacity,
        float wear,
        float dirt,
        double dashLength,
        double gapLength,
        double heightOffset,
        double lateralOffset,
        double cornerRadius,
        int renderOrder,
        boolean collision
) {
    public static final MarkingStyle DEFAULT = new MarkingStyle(
            0.12, 0xFFFFFFFF, "standard", 1.0F, 0.0F, 0.0F,
            3.0, 6.0, 0.0125, 0.0, 0.0, 0, false
    );

    public MarkingStyle normalized() {
        return new MarkingStyle(
                Math.clamp(width, 0.02, 8.0),
                color,
                material == null || material.isBlank() ? "standard" : material,
                Math.clamp(opacity, 0.0F, 1.0F),
                Math.clamp(wear, 0.0F, 1.0F),
                Math.clamp(dirt, 0.0F, 1.0F),
                Math.clamp(dashLength, 0.05, 128.0),
                Math.clamp(gapLength, 0.0, 128.0),
                Math.clamp(heightOffset, 0.001, 1.0),
                Math.clamp(lateralOffset, -32.0, 32.0),
                Math.clamp(cornerRadius, 0.0, 64.0),
                Math.clamp(renderOrder, -64, 64),
                collision
        );
    }

    public NbtCompound toNbt() {
        NbtCompound nbt = new NbtCompound();
        nbt.putDouble("width", width);
        nbt.putInt("color", color);
        nbt.putString("material", material);
        nbt.putFloat("opacity", opacity);
        nbt.putFloat("wear", wear);
        nbt.putFloat("dirt", dirt);
        nbt.putDouble("dash_length", dashLength);
        nbt.putDouble("gap_length", gapLength);
        nbt.putDouble("height_offset", heightOffset);
        nbt.putDouble("lateral_offset", lateralOffset);
        nbt.putDouble("corner_radius", cornerRadius);
        nbt.putInt("render_order", renderOrder);
        nbt.putBoolean("collision", collision);
        return nbt;
    }

    public static MarkingStyle fromNbt(NbtCompound nbt) {
        return new MarkingStyle(
                nbt.getDouble("width", DEFAULT.width),
                nbt.getInt("color", DEFAULT.color),
                nbt.getString("material", DEFAULT.material),
                nbt.getFloat("opacity", DEFAULT.opacity),
                nbt.getFloat("wear", DEFAULT.wear),
                nbt.getFloat("dirt", DEFAULT.dirt),
                nbt.getDouble("dash_length", DEFAULT.dashLength),
                nbt.getDouble("gap_length", DEFAULT.gapLength),
                nbt.getDouble("height_offset", DEFAULT.heightOffset),
                nbt.getDouble("lateral_offset", DEFAULT.lateralOffset),
                nbt.getDouble("corner_radius", DEFAULT.cornerRadius),
                nbt.getInt("render_order", DEFAULT.renderOrder),
                nbt.getBoolean("collision", DEFAULT.collision)
        ).normalized();
    }
}

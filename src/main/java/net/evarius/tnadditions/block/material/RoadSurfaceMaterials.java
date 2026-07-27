package net.evarius.tnadditions.block.material;

import net.minecraft.block.Blocks;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;

public final class RoadSurfaceMaterials {
    public static final RoadSurfaceMaterial ASPHALT = new RoadSurfaceMaterial(
            Identifier.of("terranexus", "block/road_surface/asphalt"),
            0xFF343434,
            Blocks.GRAY_CONCRETE,
            2.2F,
            6.0F,
            BlockSoundGroup.STONE
    );

    public static final RoadSurfaceMaterial WORN_ASPHALT = new RoadSurfaceMaterial(
            Identifier.of("terranexus", "block/road_surface/worn_asphalt"),
            0xFF484542,
            Blocks.GRAY_TERRACOTTA,
            2.0F,
            5.5F,
            BlockSoundGroup.STONE
    );

    private RoadSurfaceMaterials() {
    }
}

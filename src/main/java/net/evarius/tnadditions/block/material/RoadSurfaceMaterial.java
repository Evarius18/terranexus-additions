package net.evarius.tnadditions.block.material;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;

/**
 * Single source of truth for the physical properties, map color and texture
 * identity of a road surface. Resource models refer to the same texture id.
 */
public record RoadSurfaceMaterial(
        Identifier texture,
        int baseColor,
        Block mapColorSource,
        float hardness,
        float resistance,
        BlockSoundGroup sounds
) {
    public AbstractBlock.Settings settings() {
        return AbstractBlock.Settings.create()
                .mapColor(mapColorSource.getDefaultMapColor())
                .strength(hardness, resistance)
                .requiresTool()
                .sounds(sounds);
    }
}

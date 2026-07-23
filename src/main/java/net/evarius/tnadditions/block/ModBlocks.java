package net.evarius.tnadditions.block;

import net.evarius.tnadditions.TerraNexusAdditions;
import net.evarius.tnadditions.block.custom.DelineatorBlock;
import net.evarius.tnadditions.block.custom.GuardrailBlock;
import net.evarius.tnadditions.block.custom.OpenableManholeBlock;
import net.evarius.tnadditions.block.custom.RoadFurnitureBlock;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.GlazedTerracottaBlock;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;

import java.util.function.Function;

public final class ModBlocks {
    public static final String LEGACY_NAMESPACE = "terranexus";

    public static final Block ASPHALT = register("asphalt", stone(2.2f, 6f));
    public static final Block ASPHALT_SLAB = register("asphalt_slab", stone(2.2f, 6f), SlabBlock::new);
    public static final Block ASPHALT_STAIRS = register("asphalt_stairs", stone(2.2f, 6f),
            settings -> new StairsBlock(ASPHALT.getDefaultState(), settings));
    public static final Block WORN_ASPHALT = register("worn_asphalt", stone(2f, 5.5f));
    public static final Block WORN_ASPHALT_SLAB = register("worn_asphalt_slab", stone(2f, 5.5f), SlabBlock::new);
    public static final Block WORN_ASPHALT_STAIRS = register("worn_asphalt_stairs", stone(2f, 5.5f),
            settings -> new StairsBlock(WORN_ASPHALT.getDefaultState(), settings));
    public static final Block WHITE_LINE_ASPHALT = register("white_line_asphalt", stone(2.2f, 6f));
    public static final Block YELLOW_LINE_ASPHALT = register("yellow_line_asphalt", stone(2.2f, 6f));
    public static final Block CONSTRUCTION_SAND = register("construction_sand",
            AbstractBlock.Settings.create().strength(0.6f).sounds(BlockSoundGroup.SAND));
    public static final Block CRUSHED_STONE = register("crushed_stone",
            AbstractBlock.Settings.create().strength(0.8f).sounds(BlockSoundGroup.GRAVEL));
    public static final Block MILLED_ASPHALT = register("milled_asphalt",
            AbstractBlock.Settings.create().strength(0.9f).sounds(BlockSoundGroup.GRAVEL));
    public static final Block ROLLED_GRIT = register("rolled_grit",
            AbstractBlock.Settings.create().strength(0.8f).sounds(BlockSoundGroup.GRAVEL));
    public static final Block TAR = register("tar",
            AbstractBlock.Settings.create().strength(1.4f).sounds(BlockSoundGroup.MUD));
    public static final Block CONSTRUCTION_BARRIER = register("construction_barrier",
            AbstractBlock.Settings.create().strength(1.2f).sounds(BlockSoundGroup.WOOD).nonOpaque(), RoadFurnitureBlock::new);
    public static final Block DELINEATOR = register("delineator",
            AbstractBlock.Settings.create().strength(1f).sounds(BlockSoundGroup.STONE).nonOpaque(), DelineatorBlock::new);
    public static final Block DELINEATOR_LEFT = register("delineator_left",
            AbstractBlock.Settings.create().strength(1f).sounds(BlockSoundGroup.STONE).nonOpaque(), DelineatorBlock::new);
    public static final Block GUARDRAIL = register("guardrail",
            AbstractBlock.Settings.create().strength(2f).requiresTool().sounds(BlockSoundGroup.METAL).nonOpaque(), GuardrailBlock::new);
    public static final Block GUARDRAIL_END = register("guardrail_end",
            AbstractBlock.Settings.create().strength(2f).requiresTool().sounds(BlockSoundGroup.METAL).nonOpaque(), GuardrailBlock::new);
    public static final Block BRIDGE_GUARDRAIL = register("bridge_guardrail",
            AbstractBlock.Settings.create().strength(3f).requiresTool().sounds(BlockSoundGroup.METAL).nonOpaque(), GuardrailBlock::new);
    public static final Block BRIDGE_CONCRETE = register("bridge_concrete", stone(4f, 8f));
    public static final Block BRIDGE_CONCRETE_SLAB = register("bridge_concrete_slab", stone(4f, 8f), SlabBlock::new);
    public static final Block BRIDGE_STEEL = register("bridge_steel",
            AbstractBlock.Settings.create().strength(5f, 9f).requiresTool().sounds(BlockSoundGroup.METAL));
    public static final Block BRIDGE_EXPANSION_JOINT = register("bridge_expansion_joint",
            AbstractBlock.Settings.create().strength(4f, 8f).requiresTool().sounds(BlockSoundGroup.METAL));
    public static final Block ROAD_MANHOLE_D400 = register("road_manhole_d400",
            AbstractBlock.Settings.create().strength(5f, 10f).requiresTool().sounds(BlockSoundGroup.METAL).nonOpaque(), OpenableManholeBlock::new);
    public static final Block PATH_MANHOLE_B125 = register("path_manhole_b125",
            AbstractBlock.Settings.create().strength(4f, 8f).requiresTool().sounds(BlockSoundGroup.METAL).nonOpaque(), OpenableManholeBlock::new);
    public static final Block STREET_DRAIN_C250 = register("street_drain_c250",
            AbstractBlock.Settings.create().strength(4f, 8f).requiresTool().sounds(BlockSoundGroup.METAL), GlazedTerracottaBlock::new);
    public static final Block CURB_DRAIN_C250 = register("curb_drain_c250",
            AbstractBlock.Settings.create().strength(4f, 8f).requiresTool().sounds(BlockSoundGroup.METAL), GlazedTerracottaBlock::new);
    public static final Block DRAINAGE_CHANNEL_B125 = register("drainage_channel_b125",
            AbstractBlock.Settings.create().strength(3f, 7f).requiresTool().sounds(BlockSoundGroup.METAL), GlazedTerracottaBlock::new);

    private static AbstractBlock.Settings stone(float hardness, float resistance) {
        return AbstractBlock.Settings.create().strength(hardness, resistance).requiresTool().sounds(BlockSoundGroup.STONE);
    }

    private static Block register(String name, AbstractBlock.Settings settings) {
        return register(name, settings, Block::new);
    }

    private static Block register(String name, AbstractBlock.Settings settings,
                                  Function<AbstractBlock.Settings, Block> factory) {
        Identifier id = Identifier.of(LEGACY_NAMESPACE, name);
        RegistryKey<Block> blockKey = RegistryKey.of(RegistryKeys.BLOCK, id);
        RegistryKey<Item> itemKey = RegistryKey.of(RegistryKeys.ITEM, id);
        Block block = factory.apply(settings.registryKey(blockKey));
        Registry.register(Registries.ITEM, itemKey, new BlockItem(block, new Item.Settings().registryKey(itemKey)));
        return Registry.register(Registries.BLOCK, blockKey, block);
    }

    public static void registerModBlocks() {
        TerraNexusAdditions.LOGGER.info("Registering road construction blocks under legacy namespace {}", LEGACY_NAMESPACE);
    }

    private ModBlocks() {
    }
}

package net.evarius.tnadditions.block;

import net.evarius.tnadditions.TerraNexusAdditions;
import net.evarius.tnadditions.block.custom.BlinkingTemporaryBarrierBlock;
import net.evarius.tnadditions.block.custom.GuardrailBlock;
import net.evarius.tnadditions.block.custom.GuardrailVariant;
import net.evarius.tnadditions.block.custom.GullyBlock;
import net.evarius.tnadditions.block.custom.HydrantBlock;
import net.evarius.tnadditions.block.custom.LeitpfostenBlock;
import net.evarius.tnadditions.block.custom.OxidizingGuardrailBlock;
import net.evarius.tnadditions.block.custom.RoadFurnitureBlock;
import net.evarius.tnadditions.block.custom.TemporaryBarrierBlock;
import net.fabricmc.fabric.api.registry.OxidizableBlocksRegistry;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.GlazedTerracottaBlock;
import net.minecraft.block.Oxidizable;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.WallBlock;
import net.minecraft.block.enums.NoteBlockInstrument;
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

    // Asphalt
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
    public static final Block ASPHALT_MIT_REGENLAUF = register("asphalt_mit_regenlauf",
            detailedRoadSurface(2.2f, 6f), GlazedTerracottaBlock::new);
    public static final Block ASPHALT_MIT_REGENLAUF_SCHACHT = register("asphalt_mit_regenlauf_schacht",
            detailedRoadSurface(2.2f, 6f), GlazedTerracottaBlock::new);
    public static final Block SCHACHTDECKEL_ASPHAL = register("schachtdeckel_asphal",
            stone(2.2f, 6f), GlazedTerracottaBlock::new);
    public static final Block PFLASTERSTEINE = register("pflastersteine", stone(1.8f, 6f));

    // Construction Materials
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

    // Construction Barriers
    public static final Block CONSTRUCTION_BARRIER = register("construction_barrier",
            AbstractBlock.Settings.create().strength(1.2f).sounds(BlockSoundGroup.WOOD).nonOpaque(), RoadFurnitureBlock::new);
    public static final Block BARKE = register("barke",
            temporaryBarrierSettings(), settings -> temporaryBarrier(TemporaryBarrierBlock.Profile.BARKE, settings));
    public static final Block BARKE_FUSS = register("barke_fuss",
            temporaryBarrierSettings(), settings -> temporaryBarrier(TemporaryBarrierBlock.Profile.BARKE_FOOT, settings));
    public static final Block BARKE_LICHT = register("barke_licht",
            illuminatedBarrierSettings(), settings -> blinkingBarrier(TemporaryBarrierBlock.Profile.BARKE_LIGHT, settings));
    public static final Block BARKE_GROSS = register("barke_gross",
            temporaryBarrierSettings(), settings -> temporaryBarrier(TemporaryBarrierBlock.Profile.LARGE_BARKE, settings));
    public static final Block BARKE_GROSS_LICHT = register("barke_gross_licht",
            illuminatedBarrierSettings(), settings -> blinkingBarrier(TemporaryBarrierBlock.Profile.LARGE_BARKE_LIGHT, settings));
    public static final Block BAUZAUN = register("bauzaun",
            temporaryBarrierSettings(), settings -> temporaryBarrier(TemporaryBarrierBlock.Profile.CONSTRUCTION_FENCE, settings));
    public static final Block BAUZAUN_MIT_PLANE = register("bauzaun_mit_plane",
            temporaryBarrierSettings(), settings -> temporaryBarrier(TemporaryBarrierBlock.Profile.CONSTRUCTION_FENCE, settings));
    public static final Block LEUCHTE = register("leuchte",
            illuminatedBarrierSettings(), settings -> blinkingBarrier(TemporaryBarrierBlock.Profile.WARNING_LIGHT, settings));

    // Delineators & Roadside Posts
    public static final Block LEITPFOSTEN = register("leitpfosten",
            delineatorSettings(), LeitpfostenBlock::new);
    public static final Block LEITPFOSTEN_GELB = register("leitpfosten_gelb",
            delineatorSettings(), LeitpfostenBlock::new);
    public static final Block LEITPFOSTEN_WILDWARNER = register("leitpfosten_wildwarner",
            delineatorSettings(), LeitpfostenBlock::new);

    // Guardrails
    public static final Block GUARDRAIL = register("guardrail",
            guardrailSettings(), settings -> guardrail(Oxidizable.OxidationLevel.UNAFFECTED, GuardrailVariant.STANDARD, settings));
    public static final Block LIGHTLY_RUSTED_GUARDRAIL = register("lightly_rusted_guardrail",
            guardrailSettings(), settings -> guardrail(Oxidizable.OxidationLevel.EXPOSED, GuardrailVariant.STANDARD, settings));
    public static final Block HEAVILY_RUSTED_GUARDRAIL = register("heavily_rusted_guardrail",
            guardrailSettings(), settings -> guardrail(Oxidizable.OxidationLevel.WEATHERED, GuardrailVariant.STANDARD, settings));

    public static final Block GUARDRAIL_CENTER_POST = register("guardrail_center_post",
            guardrailSettings(), settings -> guardrail(Oxidizable.OxidationLevel.UNAFFECTED, GuardrailVariant.CENTER_POST, settings));
    public static final Block LIGHTLY_RUSTED_GUARDRAIL_CENTER_POST = register("lightly_rusted_guardrail_center_post",
            guardrailSettings(), settings -> guardrail(Oxidizable.OxidationLevel.EXPOSED, GuardrailVariant.CENTER_POST, settings));
    public static final Block HEAVILY_RUSTED_GUARDRAIL_CENTER_POST = register("heavily_rusted_guardrail_center_post",
            guardrailSettings(), settings -> guardrail(Oxidizable.OxidationLevel.WEATHERED, GuardrailVariant.CENTER_POST, settings));

    public static final Block GUARDRAIL_WITHOUT_POSTS = register("guardrail_without_posts",
            guardrailSettings(), settings -> guardrail(Oxidizable.OxidationLevel.UNAFFECTED, GuardrailVariant.WITHOUT_POSTS, settings));
    public static final Block LIGHTLY_RUSTED_GUARDRAIL_WITHOUT_POSTS = register("lightly_rusted_guardrail_without_posts",
            guardrailSettings(), settings -> guardrail(Oxidizable.OxidationLevel.EXPOSED, GuardrailVariant.WITHOUT_POSTS, settings));
    public static final Block HEAVILY_RUSTED_GUARDRAIL_WITHOUT_POSTS = register("heavily_rusted_guardrail_without_posts",
            guardrailSettings(), settings -> guardrail(Oxidizable.OxidationLevel.WEATHERED, GuardrailVariant.WITHOUT_POSTS, settings));

    public static final Block GUARDRAIL_END_LEFT = register("guardrail_end_left",
            guardrailSettings(), settings -> guardrail(Oxidizable.OxidationLevel.UNAFFECTED, GuardrailVariant.END_LEFT, settings));
    public static final Block LIGHTLY_RUSTED_GUARDRAIL_END_LEFT = register("lightly_rusted_guardrail_end_left",
            guardrailSettings(), settings -> guardrail(Oxidizable.OxidationLevel.EXPOSED, GuardrailVariant.END_LEFT, settings));
    public static final Block HEAVILY_RUSTED_GUARDRAIL_END_LEFT = register("heavily_rusted_guardrail_end_left",
            guardrailSettings(), settings -> guardrail(Oxidizable.OxidationLevel.WEATHERED, GuardrailVariant.END_LEFT, settings));

    public static final Block GUARDRAIL_END_RIGHT = register("guardrail_end_right",
            guardrailSettings(), settings -> guardrail(Oxidizable.OxidationLevel.UNAFFECTED, GuardrailVariant.END_RIGHT, settings));
    public static final Block LIGHTLY_RUSTED_GUARDRAIL_END_RIGHT = register("lightly_rusted_guardrail_end_right",
            guardrailSettings(), settings -> guardrail(Oxidizable.OxidationLevel.EXPOSED, GuardrailVariant.END_RIGHT, settings));
    public static final Block HEAVILY_RUSTED_GUARDRAIL_END_RIGHT = register("heavily_rusted_guardrail_end_right",
            guardrailSettings(), settings -> guardrail(Oxidizable.OxidationLevel.WEATHERED, GuardrailVariant.END_RIGHT, settings));

    public static final Block GUARDRAIL_END = register("guardrail_end",
            AbstractBlock.Settings.create().strength(2f).requiresTool().sounds(BlockSoundGroup.METAL).nonOpaque(), GuardrailBlock::new);
    public static final Block BRIDGE_GUARDRAIL = register("bridge_guardrail",
            AbstractBlock.Settings.create().strength(3f).requiresTool().sounds(BlockSoundGroup.METAL).nonOpaque(), GuardrailBlock::new);

    // Bridge
    public static final Block BRIDGE_CONCRETE = register("bridge_concrete", stone(4f, 8f));
    public static final Block BRIDGE_CONCRETE_SLAB = register("bridge_concrete_slab", stone(4f, 8f), SlabBlock::new);
    public static final Block BRIDGE_STEEL = register("bridge_steel",
            AbstractBlock.Settings.create().strength(5f, 9f).requiresTool().sounds(BlockSoundGroup.METAL));
    public static final Block BRIDGE_EXPANSION_JOINT = register("bridge_expansion_joint",
            AbstractBlock.Settings.create().strength(4f, 8f).requiresTool().sounds(BlockSoundGroup.METAL));

    // Drainage & Manholes
    public static final Block GULLY = register("gully",
            AbstractBlock.Settings.create().strength(4f, 8f).requiresTool().sounds(BlockSoundGroup.METAL).nonOpaque(),
            GullyBlock::new);
    public static final Block OBERFLURHYDRANT = register("oberflurhydrant",
            AbstractBlock.Settings.create().strength(4f, 8f).requiresTool().sounds(BlockSoundGroup.METAL).nonOpaque(),
            HydrantBlock::new);


    // Logs as Walls
    public static final Block OAK_LOG_WALL = registerLogWall("oak_log_wall", Blocks.OAK_LOG);
    public static final Block SPRUCE_LOG_WALL = registerLogWall("spruce_log_wall", Blocks.SPRUCE_LOG);
    public static final Block BIRCH_LOG_WALL = registerLogWall("birch_log_wall", Blocks.BIRCH_LOG);
    public static final Block JUNGLE_LOG_WALL = registerLogWall("jungle_log_wall", Blocks.JUNGLE_LOG);
    public static final Block ACACIA_LOG_WALL = registerLogWall("acacia_log_wall", Blocks.ACACIA_LOG);
    public static final Block DARK_OAK_LOG_WALL = registerLogWall("dark_oak_log_wall", Blocks.DARK_OAK_LOG);
    public static final Block MANGROVE_LOG_WALL = registerLogWall("mangrove_log_wall", Blocks.MANGROVE_LOG);
    public static final Block CHERRY_LOG_WALL = registerLogWall("cherry_log_wall", Blocks.CHERRY_LOG);
    public static final Block PALE_OAK_LOG_WALL = registerLogWall("pale_oak_log_wall", Blocks.PALE_OAK_LOG);

    private static AbstractBlock.Settings stone(float hardness, float resistance) {
        return AbstractBlock.Settings.create().strength(hardness, resistance).requiresTool().sounds(BlockSoundGroup.STONE);
    }

    private static AbstractBlock.Settings detailedRoadSurface(float hardness, float resistance) {
        return stone(hardness, resistance).nonOpaque();
    }

    private static AbstractBlock.Settings guardrailSettings() {
        return AbstractBlock.Settings.create()
                .strength(2f)
                .requiresTool()
                .sounds(BlockSoundGroup.METAL)
                .nonOpaque()
                .ticksRandomly();
    }

    private static AbstractBlock.Settings temporaryBarrierSettings() {
        return AbstractBlock.Settings.create()
                .strength(1.5f)
                .requiresTool()
                .sounds(BlockSoundGroup.METAL)
                .nonOpaque();
    }

    private static AbstractBlock.Settings illuminatedBarrierSettings() {
        return temporaryBarrierSettings().luminance(state ->
                state.contains(BlinkingTemporaryBarrierBlock.LIT)
                        && state.get(BlinkingTemporaryBarrierBlock.LIT) ? 12 : 0);
    }

    private static AbstractBlock.Settings delineatorSettings() {
        return AbstractBlock.Settings.create()
                .strength(1f)
                .sounds(BlockSoundGroup.STONE)
                .nonOpaque();
    }

    private static Block temporaryBarrier(
            TemporaryBarrierBlock.Profile profile,
            AbstractBlock.Settings settings
    ) {
        return new TemporaryBarrierBlock(profile, settings);
    }

    private static Block blinkingBarrier(
            TemporaryBarrierBlock.Profile profile,
            AbstractBlock.Settings settings
    ) {
        return new BlinkingTemporaryBarrierBlock(profile, settings);
    }

    private static Block guardrail(
            Oxidizable.OxidationLevel oxidationLevel,
            GuardrailVariant variant,
            AbstractBlock.Settings settings
    ) {
        return new OxidizingGuardrailBlock(oxidationLevel, variant, settings);
    }

    private static Block registerLogWall(String name, Block sourceLog) {
        AbstractBlock.Settings settings = AbstractBlock.Settings.create()
                .mapColor(sourceLog.getDefaultMapColor())
                .instrument(NoteBlockInstrument.BASS)
                .strength(2f)
                .sounds(BlockSoundGroup.WOOD)
                .burnable();
        return register(name, settings, WallBlock::new);
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
        registerFlammableLogWalls();
        registerOxidationChain(GUARDRAIL, LIGHTLY_RUSTED_GUARDRAIL, HEAVILY_RUSTED_GUARDRAIL);
        registerOxidationChain(
                GUARDRAIL_CENTER_POST,
                LIGHTLY_RUSTED_GUARDRAIL_CENTER_POST,
                HEAVILY_RUSTED_GUARDRAIL_CENTER_POST
        );
        registerOxidationChain(
                GUARDRAIL_WITHOUT_POSTS,
                LIGHTLY_RUSTED_GUARDRAIL_WITHOUT_POSTS,
                HEAVILY_RUSTED_GUARDRAIL_WITHOUT_POSTS
        );
        registerOxidationChain(
                GUARDRAIL_END_LEFT,
                LIGHTLY_RUSTED_GUARDRAIL_END_LEFT,
                HEAVILY_RUSTED_GUARDRAIL_END_LEFT
        );
        registerOxidationChain(
                GUARDRAIL_END_RIGHT,
                LIGHTLY_RUSTED_GUARDRAIL_END_RIGHT,
                HEAVILY_RUSTED_GUARDRAIL_END_RIGHT
        );
        TerraNexusAdditions.LOGGER.info("Registering road construction blocks under legacy namespace {}", LEGACY_NAMESPACE);
    }

    private static void registerFlammableLogWalls() {
        FlammableBlockRegistry flammability = FlammableBlockRegistry.getDefaultInstance();
        flammability.add(OAK_LOG_WALL, 5, 5);
        flammability.add(SPRUCE_LOG_WALL, 5, 5);
        flammability.add(BIRCH_LOG_WALL, 5, 5);
        flammability.add(JUNGLE_LOG_WALL, 5, 5);
        flammability.add(ACACIA_LOG_WALL, 5, 5);
        flammability.add(DARK_OAK_LOG_WALL, 5, 5);
        flammability.add(MANGROVE_LOG_WALL, 5, 5);
        flammability.add(CHERRY_LOG_WALL, 5, 5);
        flammability.add(PALE_OAK_LOG_WALL, 5, 5);
    }

    private static void registerOxidationChain(Block unaffected, Block exposed, Block weathered) {
        OxidizableBlocksRegistry.registerOxidizableBlockPair(unaffected, exposed);
        OxidizableBlocksRegistry.registerOxidizableBlockPair(exposed, weathered);
    }

    private ModBlocks() {
    }
}

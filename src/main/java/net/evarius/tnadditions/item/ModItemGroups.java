package net.evarius.tnadditions.item;

import net.evarius.tnadditions.TerraNexusAdditions;
import net.evarius.tnadditions.block.ModBlocks;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public final class ModItemGroups {
    public static final ItemGroup ROAD_CONSTRUCTION = Registry.register(
            Registries.ITEM_GROUP,
            Identifier.of(ModBlocks.LEGACY_NAMESPACE, "roleplay_building"),
            FabricItemGroup.builder()
                    .icon(() -> new ItemStack(ModBlocks.ASPHALT))
                    .displayName(Text.translatable("itemgroup.terranexus.roleplay_building"))
                    .entries((context, entries) -> {
                        entries.add(ModBlocks.ASPHALT);
                        entries.add(ModBlocks.ASPHALT_SLAB);
                        entries.add(ModBlocks.ASPHALT_STAIRS);
                        entries.add(ModBlocks.WORN_ASPHALT);
                        entries.add(ModBlocks.WORN_ASPHALT_SLAB);
                        entries.add(ModBlocks.WORN_ASPHALT_STAIRS);
                        entries.add(ModBlocks.WHITE_LINE_ASPHALT);
                        entries.add(ModBlocks.YELLOW_LINE_ASPHALT);
                        entries.add(ModBlocks.CONSTRUCTION_SAND);
                        entries.add(ModBlocks.CRUSHED_STONE);
                        entries.add(ModBlocks.MILLED_ASPHALT);
                        entries.add(ModBlocks.ROLLED_GRIT);
                        entries.add(ModBlocks.TAR);
                        entries.add(ModBlocks.DELINEATOR);
                        entries.add(ModBlocks.DELINEATOR_LEFT);
                        entries.add(ModBlocks.GUARDRAIL);
                        entries.add(ModBlocks.LIGHTLY_RUSTED_GUARDRAIL);
                        entries.add(ModBlocks.HEAVILY_RUSTED_GUARDRAIL);
                        entries.add(ModBlocks.GUARDRAIL_CENTER_POST);
                        entries.add(ModBlocks.LIGHTLY_RUSTED_GUARDRAIL_CENTER_POST);
                        entries.add(ModBlocks.HEAVILY_RUSTED_GUARDRAIL_CENTER_POST);
                        entries.add(ModBlocks.GUARDRAIL_WITHOUT_POSTS);
                        entries.add(ModBlocks.LIGHTLY_RUSTED_GUARDRAIL_WITHOUT_POSTS);
                        entries.add(ModBlocks.HEAVILY_RUSTED_GUARDRAIL_WITHOUT_POSTS);
                        entries.add(ModBlocks.GUARDRAIL_END_LEFT);
                        entries.add(ModBlocks.LIGHTLY_RUSTED_GUARDRAIL_END_LEFT);
                        entries.add(ModBlocks.HEAVILY_RUSTED_GUARDRAIL_END_LEFT);
                        entries.add(ModBlocks.GUARDRAIL_END_RIGHT);
                        entries.add(ModBlocks.LIGHTLY_RUSTED_GUARDRAIL_END_RIGHT);
                        entries.add(ModBlocks.HEAVILY_RUSTED_GUARDRAIL_END_RIGHT);
                        entries.add(ModBlocks.GUARDRAIL_END);
                        entries.add(ModBlocks.BRIDGE_GUARDRAIL);
                        entries.add(ModBlocks.BRIDGE_CONCRETE);
                        entries.add(ModBlocks.BRIDGE_CONCRETE_SLAB);
                        entries.add(ModBlocks.BRIDGE_STEEL);
                        entries.add(ModBlocks.BRIDGE_EXPANSION_JOINT);
                        entries.add(ModBlocks.ROAD_MANHOLE_D400);
                        entries.add(ModBlocks.PATH_MANHOLE_B125);
                        entries.add(ModBlocks.STREET_DRAIN_C250);
                        entries.add(ModBlocks.CURB_DRAIN_C250);
                        entries.add(ModBlocks.DRAINAGE_CHANNEL_B125);
                    }).build());

    public static final ItemGroup CONSTRUCTION_BARRIERS = Registry.register(
            Registries.ITEM_GROUP,
            Identifier.of(ModBlocks.LEGACY_NAMESPACE, "construction_barriers"),
            FabricItemGroup.builder()
                    .icon(() -> new ItemStack(ModBlocks.BARKE))
                    .displayName(Text.translatable("itemgroup.terranexus.construction_barriers"))
                    .entries((context, entries) -> {
                        entries.add(ModBlocks.CONSTRUCTION_BARRIER);
                        entries.add(ModBlocks.BARKE);
                        entries.add(ModBlocks.BARKE_FUSS);
                        entries.add(ModBlocks.BARKE_LICHT);
                        entries.add(ModBlocks.BARKE_GROSS);
                        entries.add(ModBlocks.BARKE_GROSS_LICHT);
                        entries.add(ModBlocks.BAUZAUN);
                        entries.add(ModBlocks.BAUZAUN_MIT_PLANE);
                        entries.add(ModBlocks.LEUCHTE);
                    }).build());

    public static void registerItemGroups() {
        TerraNexusAdditions.LOGGER.info("Registered TerraNexus road construction and construction barriers creative tabs");
    }

    private ModItemGroups() {
    }
}

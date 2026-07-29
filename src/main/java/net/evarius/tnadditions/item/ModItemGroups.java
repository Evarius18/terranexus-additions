package net.evarius.tnadditions.item;

import net.evarius.tnadditions.TerraNexusAdditions;
import net.evarius.tnadditions.block.ModBlocks;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
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
                        ModBlocks.ASPHALT_HEIGHTS.forEach(entries::add);
                        entries.add(ModBlocks.ASPHALT_STAIRS);
                        entries.add(ModBlocks.WORN_ASPHALT);
                        entries.add(ModBlocks.WORN_ASPHALT_SLAB);
                        entries.add(ModBlocks.WORN_ASPHALT_STAIRS);
                        entries.add(ModBlocks.WHITE_LINE_ASPHALT);
                        entries.add(ModBlocks.YELLOW_LINE_ASPHALT);
                        entries.add(ModBlocks.ASPHALT_MIT_REGENLAUF);
                        entries.add(ModBlocks.ASPHALT_MIT_REGENLAUF_SCHACHT);
                        entries.add(ModBlocks.SCHACHTDECKEL_ASPHAL);
                        entries.add(ModBlocks.PFLASTERSTEINE);
                        entries.add(ModBlocks.KOPFSTEINPFLASTER);
                        entries.add(ModBlocks.CONSTRUCTION_SAND);
                        entries.add(ModBlocks.CRUSHED_STONE);
                        entries.add(ModBlocks.MILLED_ASPHALT);
                        entries.add(ModBlocks.ROLLED_GRIT);
                        entries.add(ModBlocks.TAR);
                        entries.add(ModBlocks.LEITPFOSTEN);
                        entries.add(ModBlocks.LEITPFOSTEN_GELB);
                        entries.add(ModBlocks.LEITPFOSTEN_WILDWARNER);
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
                        entries.add(ModBlocks.GULLY);
                        entries.add(ModBlocks.GULLY_PFLASTER);
                        entries.add(ModBlocks.GULLY_KOPFSTEINPFLASTER);
                        entries.add(ModBlocks.OBERFLURHYDRANT);
                        entries.add(ModItems.ROAD_MARKING_EDITOR);
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
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.NATURAL).register(entries -> {
            entries.add(ModBlocks.OAK_LOG_WALL);
            entries.add(ModBlocks.SPRUCE_LOG_WALL);
            entries.add(ModBlocks.BIRCH_LOG_WALL);
            entries.add(ModBlocks.JUNGLE_LOG_WALL);
            entries.add(ModBlocks.ACACIA_LOG_WALL);
            entries.add(ModBlocks.DARK_OAK_LOG_WALL);
            entries.add(ModBlocks.MANGROVE_LOG_WALL);
            entries.add(ModBlocks.CHERRY_LOG_WALL);
            entries.add(ModBlocks.PALE_OAK_LOG_WALL);
        });
        TerraNexusAdditions.LOGGER.info(
                "Registered TerraNexus road construction, construction barriers and natural-block entries"
        );
    }

    private ModItemGroups() {
    }
}

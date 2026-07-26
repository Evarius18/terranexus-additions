package net.evarius.tnadditions;

import net.evarius.tnadditions.block.ModBlocks;
import net.evarius.tnadditions.config.GuardrailOxidationConfig;
import net.evarius.tnadditions.config.RoadMarkingSupportConfig;
import net.evarius.tnadditions.item.ModItemGroups;
import net.evarius.tnadditions.item.ModItems;
import net.evarius.tnadditions.marking.MarkingTypes;
import net.evarius.tnadditions.marking.network.RoadMarkingNetworking;
import net.evarius.tnadditions.marking.support.RoadMarkingSupportManager;
import net.fabricmc.api.ModInitializer;

import net.minecraft.util.Identifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TerraNexusAdditions implements ModInitializer {
	public static final String MOD_ID = "tnadditions";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		GuardrailOxidationConfig.load();
		RoadMarkingSupportConfig.load();
		ModBlocks.registerModBlocks();
		ModItems.register();
		MarkingTypes.registerDefaults();
		RoadMarkingNetworking.register();
		RoadMarkingSupportManager.register();
		ModItemGroups.registerItemGroups();
		LOGGER.info("TerraNexus road construction additions initialized");
	}

	public static Identifier id(String path) {
		return Identifier.of(MOD_ID, path);
	}
}

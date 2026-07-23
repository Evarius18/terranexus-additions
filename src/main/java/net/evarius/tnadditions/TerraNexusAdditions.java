package net.evarius.tnadditions;

import net.evarius.tnadditions.block.ModBlocks;
import net.evarius.tnadditions.config.GuardrailOxidationConfig;
import net.evarius.tnadditions.item.ModItemGroups;
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
		ModBlocks.registerModBlocks();
		ModItemGroups.registerItemGroups();
		LOGGER.info("TerraNexus road construction additions initialized");
	}

	public static Identifier id(String path) {
		return Identifier.of(MOD_ID, path);
	}
}

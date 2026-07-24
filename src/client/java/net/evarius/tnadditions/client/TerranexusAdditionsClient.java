package net.evarius.tnadditions.client;

import net.evarius.tnadditions.block.ModBlocks;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap;
import net.minecraft.client.render.BlockRenderLayer;

public class TerranexusAdditionsClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		BlockRenderLayerMap.putBlocks(
				BlockRenderLayer.CUTOUT,
				ModBlocks.GUARDRAIL_END_LEFT,
				ModBlocks.LIGHTLY_RUSTED_GUARDRAIL_END_LEFT,
				ModBlocks.HEAVILY_RUSTED_GUARDRAIL_END_LEFT,
				ModBlocks.GUARDRAIL_END_RIGHT,
				ModBlocks.LIGHTLY_RUSTED_GUARDRAIL_END_RIGHT,
				ModBlocks.HEAVILY_RUSTED_GUARDRAIL_END_RIGHT,
				ModBlocks.BARKE,
				ModBlocks.BARKE_FUSS,
				ModBlocks.BARKE_LICHT,
				ModBlocks.BARKE_GROSS,
				ModBlocks.BARKE_GROSS_LICHT,
				ModBlocks.BAUZAUN,
				ModBlocks.BAUZAUN_MIT_PLANE,
				ModBlocks.LEUCHTE
		);
	}
}

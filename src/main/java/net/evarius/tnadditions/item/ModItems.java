package net.evarius.tnadditions.item;

import net.evarius.tnadditions.block.ModBlocks;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public final class ModItems {
    private static final Identifier EDITOR_ID = Identifier.of(ModBlocks.LEGACY_NAMESPACE, "road_marking_editor");
    private static final RegistryKey<Item> EDITOR_KEY = RegistryKey.of(RegistryKeys.ITEM, EDITOR_ID);

    public static final Item ROAD_MARKING_EDITOR = Registry.register(
            Registries.ITEM,
            EDITOR_KEY,
            new Item(new Item.Settings().registryKey(EDITOR_KEY).maxCount(1))
    );

    public static void register() {
        // Class initialization performs registration.
    }

    private ModItems() {
    }
}

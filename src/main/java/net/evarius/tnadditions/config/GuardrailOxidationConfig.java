package net.evarius.tnadditions.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.evarius.tnadditions.TerraNexusAdditions;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public final class GuardrailOxidationConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_FILE = FabricLoader.getInstance()
            .getConfigDir()
            .resolve("tnadditions")
            .resolve("leitplanken")
            .resolve("oxidation.json");

    private static volatile boolean enabled = true;

    public static void load() {
        try {
            Files.createDirectories(CONFIG_FILE.getParent());
            if (Files.notExists(CONFIG_FILE)) {
                writeDefaults();
                return;
            }

            try (Reader reader = Files.newBufferedReader(CONFIG_FILE)) {
                Data data = GSON.fromJson(reader, Data.class);
                enabled = data == null || data.enabled;
            }
        } catch (IOException | RuntimeException exception) {
            enabled = true;
            TerraNexusAdditions.LOGGER.warn(
                    "Could not load guardrail oxidation config {}; using enabled=true",
                    CONFIG_FILE,
                    exception
            );
        }
    }

    public static boolean isEnabled() {
        return enabled;
    }

    private static void writeDefaults() throws IOException {
        try (Writer writer = Files.newBufferedWriter(CONFIG_FILE)) {
            GSON.toJson(new Data(), writer);
        }
    }

    private static final class Data {
        private boolean enabled = true;
    }

    private GuardrailOxidationConfig() {
    }
}

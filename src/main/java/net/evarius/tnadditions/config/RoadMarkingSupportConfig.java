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
import java.util.Locale;

public final class RoadMarkingSupportConfig {
    public enum UnsupportedBehavior { REMOVE, DISABLE, ADAPT }

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_FILE = FabricLoader.getInstance().getConfigDir()
            .resolve("tnadditions")
            .resolve("strassenmarkierungen")
            .resolve("support.json");

    private static volatile boolean enabled = true;
    private static volatile UnsupportedBehavior behavior = UnsupportedBehavior.REMOVE;
    private static volatile int checkIntervalTicks = 20;
    private static volatile double sampleSpacing = 0.5;
    private static volatile double maxAdaptDistance = 2.0;

    public static void load() {
        try {
            Files.createDirectories(CONFIG_FILE.getParent());
            if (Files.notExists(CONFIG_FILE)) {
                writeDefaults();
                return;
            }
            try (Reader reader = Files.newBufferedReader(CONFIG_FILE)) {
                Data data = GSON.fromJson(reader, Data.class);
                if (data == null) return;
                enabled = data.enabled;
                try {
                    behavior = UnsupportedBehavior.valueOf(data.unsupportedBehavior.toUpperCase(Locale.ROOT));
                } catch (IllegalArgumentException | NullPointerException ignored) {
                    behavior = UnsupportedBehavior.REMOVE;
                }
                checkIntervalTicks = Math.clamp(data.checkIntervalTicks, 5, 1200);
                sampleSpacing = Math.clamp(data.sampleSpacing, 0.1, 4.0);
                maxAdaptDistance = Math.clamp(data.maxAdaptDistance, 0.25, 16.0);
            }
        } catch (IOException | RuntimeException exception) {
            TerraNexusAdditions.LOGGER.warn("Could not load road marking support config {}; using defaults",
                    CONFIG_FILE, exception);
        }
    }

    private static void writeDefaults() throws IOException {
        try (Writer writer = Files.newBufferedWriter(CONFIG_FILE)) {
            GSON.toJson(new Data(), writer);
        }
    }

    public static boolean isEnabled() { return enabled; }
    public static UnsupportedBehavior behavior() { return behavior; }
    public static int checkIntervalTicks() { return checkIntervalTicks; }
    public static double sampleSpacing() { return sampleSpacing; }
    public static double maxAdaptDistance() { return maxAdaptDistance; }

    private static final class Data {
        private boolean enabled = true;
        private String unsupportedBehavior = "REMOVE";
        private int checkIntervalTicks = 20;
        private double sampleSpacing = 0.5;
        private double maxAdaptDistance = 2.0;
    }

    private RoadMarkingSupportConfig() {
    }
}

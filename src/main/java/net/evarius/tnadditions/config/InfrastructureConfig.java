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

public final class InfrastructureConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("tnadditions").resolve("infrastructure.json");
    private static Data data = new Data();

    public static void load() {
        try {
            Files.createDirectories(FILE.getParent());
            if (Files.exists(FILE)) try (Reader reader = Files.newBufferedReader(FILE)) {
                Data loaded = GSON.fromJson(reader, Data.class); if (loaded != null) data = loaded;
            }
            data.validate();
            try (Writer writer = Files.newBufferedWriter(FILE)) { GSON.toJson(data, writer); }
        } catch (IOException | RuntimeException exception) {
            data = new Data();
            TerraNexusAdditions.LOGGER.warn("Could not load infrastructure config {}; defaults remain active", FILE, exception);
        }
    }

    public static int maximumDevices() { return data.maximumDevices; }
    public static int maximumPrograms() { return data.maximumPrograms; }
    public static int signalUpdateTicks() { return data.signalUpdateTicks; }
    public static int garageAnimationTicks() { return data.garageAnimationTicks; }
    public static int garageRemoteRange() { return data.garageRemoteRange; }
    public static String defaultDeviceGroup() { return data.defaultDeviceGroup; }

    private static final class Data {
        int maximumDevices = 4096;
        int maximumPrograms = 64;
        int signalUpdateTicks = 10;
        int garageAnimationTicks = 2;
        int garageRemoteRange = 64;
        String defaultDeviceGroup = "default";
        void validate() {
            maximumDevices=Math.max(1,Math.min(100_000,maximumDevices)); maximumPrograms=Math.max(1,Math.min(1_000,maximumPrograms));
            signalUpdateTicks=Math.max(1,Math.min(200,signalUpdateTicks)); garageAnimationTicks=Math.max(1,Math.min(40,garageAnimationTicks));
            garageRemoteRange=Math.max(4,Math.min(512,garageRemoteRange));
            if(defaultDeviceGroup==null||defaultDeviceGroup.isBlank())defaultDeviceGroup="default";
        }
    }
    private InfrastructureConfig() {}
}

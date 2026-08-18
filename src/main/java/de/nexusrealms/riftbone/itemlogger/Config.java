package de.nexusrealms.riftbone.itemlogger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.nexusrealms.riftbone.Riftbone;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

public class Config {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG = FabricLoader.getInstance().getConfigDir().resolve("riftbone/itemlog.json");
    protected final int duration = 1;
    protected final ChronoUnit unit = ChronoUnit.DAYS;
    protected final int limit = 4;
    public static Config load(){
        Config config = new Config();
        if (Files.notExists(CONFIG)) {
            config.save();
            return config;
        }

        try (Reader reader = Files.newBufferedReader(CONFIG)) {
            Config loaded = GSON.fromJson(reader, Config.class);
            return loaded == null ? config : loaded;
        } catch (IOException | RuntimeException exception) {
            Riftbone.LOGGER.warn("Could not read {}", CONFIG, exception);
            return config;
        }
    }
    private void save() {
        try {
            Files.createDirectories(CONFIG.getParent());
            try (Writer writer = Files.newBufferedWriter(CONFIG)) {
                GSON.toJson(this, writer);
            }
        } catch (IOException exception) {
            Riftbone.LOGGER.warn("Could not create default itemlog config at {}", CONFIG, exception);
        }
    }
}

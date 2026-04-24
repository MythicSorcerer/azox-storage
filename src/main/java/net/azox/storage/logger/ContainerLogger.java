package net.azox.storage.logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.azox.storage.AzoxStorage;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public final class ContainerLogger {

    private final AzoxStorage plugin;
    private final Gson gson;
    private final SimpleDateFormat dateFormat;

    public ContainerLogger() {
        this.plugin = AzoxStorage.getInstance();
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    }

    public void logOpen(final UUID playerId, final String containerKey) {
        if (!this.plugin.getPluginConfig().isLogContainerEvents()) {
            return;
        }

        final File logFile = this.getLogFile(playerId);
        final String timestamp = this.dateFormat.format(new Date());

        final String entry = String.format("[%s] OPEN: %s%n", timestamp, containerKey);

        this.appendToFile(logFile, entry);
    }

    public void logBreak(final UUID playerId, final String containerKey) {
        if (!this.plugin.getPluginConfig().isLogContainerEvents()) {
            return;
        }

        final File logFile = this.getLogFile(playerId);
        final String timestamp = this.dateFormat.format(new Date());

        final String entry = String.format("[%s] BREAK: %s%n", timestamp, containerKey);

        this.appendToFile(logFile, entry);
    }

    private File getLogFile(final UUID playerId) {
        final Path logsDir = this.plugin.getDataFolder().toPath().resolve("logs").resolve(playerId.toString());
        try {
            Files.createDirectories(logsDir);
        } catch (final IOException e) {
            this.plugin.getLogger().warning("Could not create logs directory: " + e.getMessage());
        }

        return logsDir.resolve("containers.yml").toFile();
    }

    private void appendToFile(final File file, final String content) {
        try {
            Files.writeString(file.toPath(), content, java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND);
        } catch (final IOException e) {
            this.plugin.getLogger().warning("Could not write to log file: " + e.getMessage());
        }
    }

    public void saveAll() {
    }

    public void reload() {
    }
}
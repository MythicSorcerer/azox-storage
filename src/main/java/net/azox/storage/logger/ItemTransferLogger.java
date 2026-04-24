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

public final class ItemTransferLogger {

    private final AzoxStorage plugin;
    private final Gson gson;
    private final SimpleDateFormat dateFormat;

    public ItemTransferLogger() {
        this.plugin = AzoxStorage.getInstance();
        this.gson = new GsonBuilder().create();
        this.dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    }

    public void logPickup(final UUID playerId, final org.bukkit.inventory.ItemStack item) {
        if (!this.plugin.getPluginConfig().isLogItemTransfers()) {
            return;
        }

        final File logFile = this.getLogFile(playerId);
        final String timestamp = this.dateFormat.format(new Date());
        final String itemJson = this.gson.toJson(new ItemSerialize(item));

        final String entry = String.format("[%s] PICKUP: %s%n", timestamp, itemJson);

        this.appendToFile(logFile, entry);
    }

    public void logDrop(final UUID playerId, final org.bukkit.inventory.ItemStack item) {
        if (!this.plugin.getPluginConfig().isLogItemTransfers()) {
            return;
        }

        final File logFile = this.getLogFile(playerId);
        final String timestamp = this.dateFormat.format(new Date());
        final String itemJson = this.gson.toJson(new ItemSerialize(item));

        final String entry = String.format("[%s] DROP: %s%n", timestamp, itemJson);

        this.appendToFile(logFile, entry);
    }

    private File getLogFile(final UUID playerId) {
        final Path logsDir = this.plugin.getDataFolder().toPath().resolve("logs").resolve(playerId.toString());
        try {
            Files.createDirectories(logsDir);
        } catch (final IOException e) {
            this.plugin.getLogger().warning("Could not create logs directory: " + e.getMessage());
        }

        return logsDir.resolve("item-transfer.log").toFile();
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

    public record ItemSerialize(String material, int amount, short damage, String displayName, int customModelData) {
        public ItemSerialize(final org.bukkit.inventory.ItemStack item) {
            this(item.getType().getKey().toString(), item.getAmount(), item.getDurability(), item.hasItemMeta() ? item.getItemMeta().getDisplayName() : null, item.hasItemMeta() ? item.getItemMeta().getCustomModelData() : 0);
        }
    }
}
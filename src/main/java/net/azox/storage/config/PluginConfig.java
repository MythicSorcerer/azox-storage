package net.azox.storage.config;

import lombok.Getter;
import net.azox.storage.AzoxStorage;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

@Getter
public final class PluginConfig {

    private final AzoxStorage plugin;
    private FileConfiguration config;

    private int containerUuidLength;
    private boolean allowChestTheft;
    private int antiGraveRobberySeconds;
    private int graveDespawnTime;
    private int droppedItemDespawnTime;
    private double trialChambersReach;
    private boolean blockKeyInTrialChambers;
    private boolean logContainerEvents;
    private boolean logItemTransfers;
    private boolean perContainerLogs;
    private int maxLockedContainers;
    private int maxPresetsPerContainer;
    private int maxKeysPerContainer;
    private String prefix;

    public PluginConfig() {
        this.plugin = AzoxStorage.getInstance();
        this.load();
    }

    public void load() {
        final File file = new File(this.plugin.getDataFolder(), "config.yml");
        if (!file.exists()) {
            this.plugin.saveResource("config.yml", false);
        }

        this.config = YamlConfiguration.loadConfiguration(file);
        
        this.containerUuidLength = this.config.getInt("container-uuid-length", 5);
        this.allowChestTheft = this.config.getBoolean("allow-chest-theft", false);
        this.antiGraveRobberySeconds = this.config.getInt("anti-grave-robbery-seconds", 0);
        this.graveDespawnTime = this.config.getInt("grave-despawn-time", 300);
        this.droppedItemDespawnTime = this.config.getInt("dropped-item-despawn-time", 6000);
        this.trialChambersReach = this.config.getDouble("trial-chambers-reach", 0.0);
        this.blockKeyInTrialChambers = this.config.getBoolean("block-key-in-trial-chambers", true);
        this.logContainerEvents = this.config.getBoolean("logging.container-events", true);
        this.logItemTransfers = this.config.getBoolean("logging.item-transfers", true);
        this.perContainerLogs = this.config.getBoolean("logging.per-container-logs", false);
        this.maxLockedContainers = this.config.getInt("limits.max-locked-containers", -1);
        this.maxPresetsPerContainer = this.config.getInt("limits.max-presets-per-container", -1);
        this.maxKeysPerContainer = this.config.getInt("limits.max-keys-per-container", -1);
        this.prefix = this.config.getString("messages.prefix", "&8[&6Azox&fStorage&8] ");
    }

    public void save() {
        try {
            this.config.save(new File(this.plugin.getDataFolder(), "config.yml"));
        } catch (final Exception e) {
            this.plugin.getLogger().warning("Could not save config.yml: " + e.getMessage());
        }
    }

    public String getPrefix() {
        return ChatColor.translateAlternateColorCodes('&', this.prefix);
    }

    public boolean isBlockKeyInTrialChambers() {
        return this.blockKeyInTrialChambers;
    }
}

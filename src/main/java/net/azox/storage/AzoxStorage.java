package net.azox.storage;

import lombok.Getter;
import net.azox.storage.command.AdminCommand;
import net.azox.storage.command.BypassLocksCommand;
import net.azox.storage.command.ChestConfigCommand;
import net.azox.storage.command.VoidCommand;
import net.azox.storage.config.PluginConfig;
import net.azox.storage.container.ContainerManager;
import net.azox.storage.grave.GraveManager;
import net.azox.storage.key.KeyManager;
import net.azox.storage.listener.ContainerListener;
import net.azox.storage.listener.DropItemListener;
import net.azox.storage.listener.GraveListener;
import net.azox.storage.listener.KeyListener;
import net.azox.storage.listener.PlayerListener;
import net.azox.storage.logger.ContainerLogger;
import net.azox.storage.logger.ItemTransferLogger;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public final class AzoxStorage extends JavaPlugin {

    @Getter
    private static AzoxStorage instance;

    private PluginConfig pluginConfig;
    private ContainerManager containerManager;
    private KeyManager keyManager;
    private GraveManager graveManager;
    private ContainerLogger containerLogger;
    private ItemTransferLogger itemTransferLogger;

    @Override
    public void onEnable() {
        instance = this;
        this.loadConfig();
        this.initializeManagers();
        this.registerCommands();
        this.registerListeners();
        this.getLogger().info("Azox Storage has been enabled!");
    }

    @Override
    public void onDisable() {
        if (this.containerManager != null) {
            this.containerManager.saveAll();
        }
        if (this.graveManager != null) {
            this.graveManager.saveAll();
        }
        if (this.keyManager != null) {
            this.keyManager.saveAll();
        }
        this.getLogger().info("Azox Storage has been disabled!");
    }

    public void reloadPlugin() {
        this.reloadConfig();
        this.pluginConfig.load();
        this.containerManager.reload();
        this.keyManager.reload();
        this.graveManager.reload();
    }

    private void loadConfig() {
        this.saveDefaultConfig();
        this.pluginConfig = new PluginConfig();
        this.pluginConfig.load();
    }

    private void initializeManagers() {
        this.containerManager = new ContainerManager();
        this.keyManager = new KeyManager();
        this.graveManager = new GraveManager();
        this.containerLogger = new ContainerLogger();
        this.itemTransferLogger = new ItemTransferLogger();
    }

    private void registerCommands() {
        this.getCommand("azoxstorage").setExecutor(new AdminCommand());
        this.getCommand("bypasslocks").setExecutor(new BypassLocksCommand());
        this.getCommand("chestconfig").setExecutor(new ChestConfigCommand());
        this.getCommand("void").setExecutor(new VoidCommand());
    }

    private void registerListeners() {
        this.getServer().getPluginManager().registerEvents(new ContainerListener(), this);
        this.getServer().getPluginManager().registerEvents(new PlayerListener(), this);
        this.getServer().getPluginManager().registerEvents(new GraveListener(), this);
        this.getServer().getPluginManager().registerEvents(new DropItemListener(), this);
        this.getServer().getPluginManager().registerEvents(new KeyListener(), this);
    }
}
package net.azox.storage.container;

import lombok.Getter;
import net.azox.storage.AzoxStorage;
import net.azox.storage.data.PresetData;
import net.azox.storage.listener.PlayerListener;
import net.azox.storage.util.ContainerUUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
public final class ContainerManager {

    private final AzoxStorage plugin;
    private final Map<String, ContainerData> containers;
    private final Map<UUID, List<ContainerData>> playerContainers;
    private final Map<String, PresetData> presets;
    private FileConfiguration config;

    public ContainerManager() {
        this.plugin = AzoxStorage.getInstance();
        this.containers = new HashMap<>();
        this.playerContainers = new HashMap<>();
        this.presets = new HashMap<>();
        this.load();
    }

    public void registerContainer(final Player player, final Location location, final Material type) {
        final String containerId = ContainerUUID.generate(player.getUniqueId(), type);
        final ContainerData container = new ContainerData(
                player.getUniqueId(),
                player.getName(),
                type,
                containerId,
                location,
                true
        );

        this.containers.put(container.getUniqueKey(), container);
        this.playerContainers.computeIfAbsent(player.getUniqueId(), k -> new ArrayList<>()).add(container);

        this.renameContainer(container);
        this.save();
    }

    private void renameContainer(final ContainerData container) {
        final Block block = container.getLocation().getBlock();
        final var state = block.getState();
        if (state instanceof org.bukkit.block.Container containerBlock) {
            final String displayName = ContainerUUID.formatDisplayName(
                    container.getOwnerName(),
                    container.getType(),
                    container.getContainerId()
            );
            containerBlock.setCustomName(net.azox.storage.util.MessageUtil.color(displayName));
            containerBlock.update();
        }
    }

    public ContainerData getContainer(final Location location) {
        final String worldName = location.getWorld().getName();
        final int x = location.getBlockX();
        final int y = location.getBlockY();
        final int z = location.getBlockZ();

        for (final ContainerData container : this.containers.values()) {
            final var loc = container.getLocation();
            if (loc.getWorld().getName().equals(worldName) &&
                    loc.getBlockX() == x &&
                    loc.getBlockY() == y &&
                    loc.getBlockZ() == z) {
                return container;
            }
        }

        if (location.getBlock().getType() == Material.CHEST) {
            return this.findLinkedContainer(location);
        }
        
        return null;
    }

    private ContainerData findLinkedContainer(final Location location) {
        final var world = location.getWorld();
        final int x = location.getBlockX();
        final int y = location.getBlockY();
        final int z = location.getBlockZ();

        final Location[] adjacent = {
            new Location(world, x - 1, y, z),
            new Location(world, x + 1, y, z),
            new Location(world, x, y, z - 1),
            new Location(world, x, y, z + 1)
        };

        for (final var adj : adjacent) {
            if (adj.getBlock().getType() == Material.CHEST) {
                final String adjWorld = adj.getWorld().getName();
                final int adjX = adj.getBlockX();
                final int adjY = adj.getBlockY();
                final int adjZ = adj.getBlockZ();

                for (final ContainerData container : this.containers.values()) {
                    final var loc = container.getLocation();
                    if (loc.getWorld().getName().equals(adjWorld) &&
                            loc.getBlockX() == adjX &&
                            loc.getBlockY() == adjY &&
                            loc.getBlockZ() == adjZ) {
                        return container;
                    }
                }
            }
        }
        return null;
    }

    public ContainerData getContainer(final UUID owner, final Material type, final String containerId) {
        final String key = owner.toString() + "_" + type.name() + "_" + containerId;
        return this.containers.get(key);
    }

    public boolean canAccess(final Player player, final ContainerData container) {
        if (container == null) {
            return false;
        }

        if (container.getOwner().equals(player.getUniqueId())) {
            return true;
        }

        if (this.hasBypassPermission(player)) {
            return true;
        }

        if (container.hasPresetAccess(player.getUniqueId())) {
            return true;
        }

        if (this.plugin.getKeyManager().hasValidKey(player, container.getUniqueKey())) {
            return true;
        }

        return false;
    }

    public boolean hasBypassPermission(final Player player) {
        return player.hasPermission("azox.storage.bypass") && PlayerListener.hasBypassActivated(player);
    }

    public void addPreset(final ContainerData container, final String presetName) {
        if (this.presets.containsKey(presetName)) {
            container.addPreset(presetName);
            this.save();
        }
    }

    public void removePreset(final ContainerData container, final String presetName) {
        container.removePreset(presetName);
        this.save();
    }

    public int getContainerCount(final UUID playerId) {
        return this.playerContainers.getOrDefault(playerId, new ArrayList<>()).size();
    }

    public void unregisterContainer(final Location location) {
        final ContainerData container = this.getContainer(location);
        if (container != null) {
            this.containers.remove(container.getUniqueKey());
            final var list = this.playerContainers.get(container.getOwner());
            if (list != null) {
                list.remove(container);
            }
            this.save();
        }
    }

    public Collection<ContainerData> getContainers() {
        return this.containers.values();
    }

    public List<ContainerData> getContainers(final UUID playerId) {
        return new ArrayList<>(this.playerContainers.getOrDefault(playerId, new ArrayList<>()));
    }

    public List<PresetData> getPresets(final UUID ownerId) {
        return this.presets.values().stream()
                .filter(p -> p.getOwnerId().equals(ownerId))
                .toList();
    }

    public void load() {
        final File file = new File(this.plugin.getDataFolder(), "containers.yml");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (final IOException e) {
                this.plugin.getLogger().warning("Could not create containers.yml: " + e.getMessage());
            }
        }

        this.config = YamlConfiguration.loadConfiguration(file);
        this.containers.clear();
        this.playerContainers.clear();

        for (final String key : this.config.getKeys(false)) {
            final var section = this.config.getConfigurationSection(key);
            if (section == null) {
                continue;
            }

            final var container = this.loadContainer(section);
            if (container != null) {
                this.containers.put(container.getUniqueKey(), container);
                this.playerContainers.computeIfAbsent(container.getOwner(), k -> new ArrayList<>()).add(container);
            }
        }
    }

    private ContainerData loadContainer(final org.bukkit.configuration.ConfigurationSection section) {
        try {
            final String ownerStr = section.getString("owner", "");
            if (ownerStr == null || ownerStr.isEmpty()) {
                return null;
            }
            
            final UUID owner;
            try {
                owner = UUID.fromString(ownerStr);
            } catch (final IllegalArgumentException e) {
                this.plugin.getLogger().warning("Failed to load container: Invalid UUID string: " + ownerStr);
                return null;
            }
            
            final String ownerName = section.getString("ownerName", "");
            final String typeStr = section.getString("type", "CHEST");
            final Material type = Material.valueOf(typeStr);
            final String containerId = section.getString("containerId", "");
            final String worldName = section.getString("world", "");
            final World world = Bukkit.getWorld(worldName);
            if (world == null) {
                return null;
            }

            final int x = section.getInt("x", 0);
            final int y = section.getInt("y", 0);
            final int z = section.getInt("z", 0);
            final Location location = new Location(world, x, y, z);

            final ContainerData container = new ContainerData(owner, ownerName, type, containerId, location, true);

            final List<String> presetList = section.getStringList("presets");
            for (final String preset : presetList) {
                container.addPreset(preset);
            }

            return container;
        } catch (final Exception e) {
            this.plugin.getLogger().warning("Failed to load container: " + e.getMessage());
            return null;
        }
    }

    public void save() {
        this.config.set("containers", null);

        for (final ContainerData container : this.containers.values()) {
            final String path = "containers." + container.getUniqueKey();
            this.config.set(path + ".owner", container.getOwner().toString());
            this.config.set(path + ".ownerName", container.getOwnerName());
            this.config.set(path + ".type", container.getType().name());
            this.config.set(path + ".containerId", container.getContainerId());
            this.config.set(path + ".world", container.getLocation().getWorld().getName());
            this.config.set(path + ".x", container.getLocation().getBlockX());
            this.config.set(path + ".y", container.getLocation().getBlockY());
            this.config.set(path + ".z", container.getLocation().getBlockZ());
            this.config.set(path + ".presets", new ArrayList<>(container.getPresets()));
        }

        try {
            this.config.save(new File(this.plugin.getDataFolder(), "containers.yml"));
        } catch (final IOException e) {
            this.plugin.getLogger().warning("Could not save containers.yml: " + e.getMessage());
        }
    }

    public void saveAll() {
        this.save();
    }

    public void reload() {
        this.load();
    }
}

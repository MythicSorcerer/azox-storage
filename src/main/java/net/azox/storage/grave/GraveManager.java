package net.azox.storage.grave;

import lombok.Getter;
import net.azox.storage.AzoxStorage;
import net.azox.storage.data.GraveData;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Getter
public final class GraveManager {

    private final AzoxStorage plugin;
    private final Map<String, GraveData> graves;
    private FileConfiguration config;

    public GraveManager() {
        this.plugin = AzoxStorage.getInstance();
        this.graves = new HashMap<>();
        this.load();
    }

    public void createGrave(final Player player) {
        final Location location = this.findGraveLocation(player.getLocation());
        final ItemStack[] inventory = player.getInventory().getContents();
        final ItemStack[] armor = player.getInventory().getArmorContents();

        final var skull = new ItemStack(Material.SKELETON_SKULL, 1);
        final var chest = new ItemStack(Material.CHEST, 1);

        final var grave = new GraveData(
                player.getUniqueId(),
                player.getName(),
                location,
                inventory,
                armor,
                skull,
                chest
        );

        final String key = this.getGraveKey(location);
        this.graves.put(key, grave);

        this.buildGrave(grave);
        this.scheduleDespawn(grave);
        this.save();
    }

    private Location findGraveLocation(final Location playerLocation) {
        final World world = playerLocation.getWorld();
        final int x = playerLocation.getBlockX();
        final int y = playerLocation.getBlockY() + 1;
        final int z = playerLocation.getBlockZ();

        for (int i = 0; i < 10; i++) {
            final Location loc = new Location(world, x + i, y, z);
            if (!loc.getBlock().getType().isSolid()) {
                return loc;
            }
        }

        return playerLocation.clone().add(0, 2, 0);
    }

    private void buildGrave(final GraveData grave) {
        final Location location = grave.getLocation();

        final var block = location.getBlock();
        block.setType(Material.CHEST);

        final var skullLoc = location.clone().add(0, 1, 0);
        final var entity = location.getWorld().spawnEntity(skullLoc, EntityType.ARMOR_STAND);
        if (entity instanceof final ArmorStand armorStand) {
            armorStand.setVisible(false);
            armorStand.setMarker(true);
            armorStand.setGravity(false);
            armorStand.getEquipment().setHelmet(grave.getSkull());
            armorStand.setCustomName("Grave of " + grave.getOwnerName());
            armorStand.setCustomNameVisible(true);
        }

        grave.setArmorStand(entity.getUniqueId());
    }

    private void scheduleDespawn(final GraveData grave) {
        final int despawnTime = this.plugin.getPluginConfig().getGraveDespawnTime() * 20;

        new BukkitRunnable() {
            @Override
            public void run() {
                if (grave.isLooted()) {
                    this.cancel();
                    return;
                }

                GraveManager.this.removeGrave(grave);
            }
        }.runTaskLater(this.plugin, despawnTime);
    }

    public void quickLoot(final Player player, final Location location) {
        final String key = this.getGraveKey(location);
        final var grave = this.graves.get(key);

        if (grave == null) {
            return;
        }

        if (!grave.isOwner(player.getUniqueId()) && !player.hasPermission("azox.storage.bypass")) {
            player.sendMessage("§cThis grave belongs to someone else!");
            return;
        }

        grave.setLooted(true);

        for (final ItemStack item : grave.getInventory()) {
            if (item != null && item.getType() != Material.AIR) {
                final var loc = player.getLocation();
                loc.getWorld().dropItemNaturally(loc, item);
            }
        }

        for (final ItemStack item : grave.getArmor()) {
            if (item != null && item.getType() != Material.AIR) {
                final var loc = player.getLocation();
                loc.getWorld().dropItemNaturally(loc, item);
            }
        }

        this.removeGrave(grave);
        player.sendMessage("§aYou looted the grave!");
    }

    public void removeGrave(final GraveData grave) {
        final String key = this.getGraveKey(grave.getLocation());
        this.graves.remove(key);

        final var block = grave.getLocation().getBlock();
        block.setType(Material.AIR);

        final var entityId = grave.getArmorStand();
        if (entityId != null) {
            final var entity = this.plugin.getServer().getEntity(entityId);
            if (entity != null) {
                entity.remove();
            }
        }

        this.save();
    }

    public void markItemDespawned(final ItemStack item, final UUID lastAccessor) {
    }

    private String getGraveKey(final Location location) {
        final String worldName = location.getWorld().getName();
        final int x = location.getBlockX();
        final int y = location.getBlockY();
        final int z = location.getBlockZ();
        return worldName + "_" + x + "_" + y + "_" + z;
    }

    public boolean isGrave(final Location location) {
        return this.graves.containsKey(this.getGraveKey(location));
    }

    public GraveData getGrave(final Location location) {
        return this.graves.get(this.getGraveKey(location));
    }

    public void openVoidInventory(final org.bukkit.command.CommandSender sender) {
        sender.sendMessage("§6Opening void inventory...");
    }

    public void load() {
        final File file = new File(this.plugin.getDataFolder(), "graves.yml");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (final IOException e) {
                this.plugin.getLogger().warning("Could not create graves.yml: " + e.getMessage());
            }
        }

        this.config = YamlConfiguration.loadConfiguration(file);
        this.graves.clear();
    }

    public void save() {
        this.config.set("graves", null);

        for (final Map.Entry<String, GraveData> entry : this.graves.entrySet()) {
            final var grave = entry.getValue();
            final String path = "graves." + entry.getKey();
            this.config.set(path + ".owner", grave.getOwnerId().toString());
            this.config.set(path + ".ownerName", grave.getOwnerName());
            this.config.set(path + ".world", grave.getLocation().getWorld().getName());
            this.config.set(path + ".x", grave.getLocation().getBlockX());
            this.config.set(path + ".y", grave.getLocation().getBlockY());
            this.config.set(path + ".z", grave.getLocation().getBlockZ());
        }

        try {
            this.config.save(new File(this.plugin.getDataFolder(), "graves.yml"));
        } catch (final IOException e) {
            this.plugin.getLogger().warning("Could not save graves.yml: " + e.getMessage());
        }
    }

    public void saveAll() {
        this.save();
    }

    public void reload() {
        this.load();
    }
}

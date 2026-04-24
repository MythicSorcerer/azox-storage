package net.azox.storage.listener;

import org.bukkit.Location;
import net.azox.storage.AzoxStorage;
import net.azox.storage.container.ContainerData;
import net.azox.storage.util.MessageUtil;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Set;

public final class ContainerListener implements Listener {

    private static final Set<Material> CONTAINER_TYPES = Set.of(
            Material.CHEST,
            Material.BARREL,
            Material.TRAPPED_CHEST
    );

    private final AzoxStorage plugin;

    public ContainerListener() {
        this.plugin = AzoxStorage.getInstance();
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockPlace(final BlockPlaceEvent event) {
        final Player player = event.getPlayer();
        final var block = event.getBlock();
        final var type = block.getType();

        if (!CONTAINER_TYPES.contains(type)) {
            return;
        }

        final var location = block.getLocation();
        final var containerManager = this.plugin.getContainerManager();
        final var existingContainer = containerManager.getContainer(location);

        if (existingContainer != null) {
            return;
        }

        if (type == Material.CHEST) {
            final var adjacent = this.findLinkedChest(location);
            if (adjacent != null) {
                return;
            }
        }

        containerManager.registerContainer(player, location, type);
        player.sendMessage("§aContainer registered as your locked container!");
    }

    private Location findLinkedChest(final Location location) {
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

        for (final var loc : adjacent) {
            if (loc.getBlock().getType() == Material.CHEST) {
                for (final var container : this.plugin.getContainerManager().getContainers()) {
                    final var cLoc = container.getLocation();
                    if (cLoc.getWorld().getName().equals(loc.getWorld().getName()) &&
                            cLoc.getBlockX() == loc.getBlockX() &&
                            cLoc.getBlockY() == loc.getBlockY() &&
                            cLoc.getBlockZ() == loc.getBlockZ()) {
                        return loc;
                    }
                }
            }
        }
        return null;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(final BlockBreakEvent event) {
        final Player player = event.getPlayer();
        final var block = event.getBlock();
        final var type = block.getType();

        if (!CONTAINER_TYPES.contains(type)) {
            return;
        }

        final var container = this.plugin.getContainerManager().getContainer(block.getLocation());

        if (container == null) {
            return;
        }

        if (container.getOwner().equals(player.getUniqueId())) {
            this.plugin.getContainerManager().unregisterContainer(block.getLocation());
            return;
        }

        if (!this.plugin.getPluginConfig().isAllowChestTheft()) {
            if (this.plugin.getContainerManager().hasBypassPermission(player)) {
                return;
            }

            event.setCancelled(true);
            player.sendMessage("§cThis container is locked by another player!");
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(final PlayerInteractEvent event) {
        final Player player = event.getPlayer();

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_AIR) {
            return;
        }

        final var block = event.getClickedBlock();
        if (block == null) {
            return;
        }

        final var type = block.getType();
        if (!CONTAINER_TYPES.contains(type)) {
            return;
        }

        var container = this.plugin.getContainerManager().getContainer(block.getLocation());

        if (container == null && type == Material.CHEST) {
            container = this.findLinkedContainer(block.getLocation());
        }

        if (container == null) {
            return;
        }

        if (player.isSneaking()) {
            event.setCancelled(true);
            return;
        }

        if (this.plugin.getContainerManager().canAccess(player, container)) {
            player.sendMessage("§aOpening container: §e" + container.getContainerId());
            return;
        }

        event.setCancelled(true);
        player.sendMessage("§cThis container is locked!");
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

        for (final var loc : adjacent) {
            if (loc.getBlock().getType() == Material.CHEST) {
                for (final var c : this.plugin.getContainerManager().getContainers()) {
                    final var cLoc = c.getLocation();
                    if (cLoc.getWorld().getName().equals(loc.getWorld().getName()) &&
                            cLoc.getBlockX() == loc.getBlockX() &&
                            cLoc.getBlockY() == loc.getBlockY() &&
                            cLoc.getBlockZ() == loc.getBlockZ()) {
                        return c;
                    }
                }
            }
        }
        return null;
    }
}

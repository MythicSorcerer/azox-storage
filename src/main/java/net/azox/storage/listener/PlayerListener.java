package net.azox.storage.listener;

import net.azox.storage.AzoxStorage;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class PlayerListener implements Listener {

    private static final Set<UUID> BYPASS_ACTIVATED = new HashSet<>();

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerQuit(final PlayerQuitEvent event) {
        final Player player = event.getPlayer();
        BYPASS_ACTIVATED.remove(player.getUniqueId());
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerDropItem(final PlayerDropItemEvent event) {
        final Player player = event.getPlayer();
        final ItemStack item = event.getItemDrop().getItemStack();

        final AzoxStorage plugin = AzoxStorage.getInstance();
        plugin.getKeyManager().trackDroppedKey(player, item);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onInventoryClose(final InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof final Player player)) {
            return;
        }

        final AzoxStorage plugin = AzoxStorage.getInstance();
        plugin.getKeyManager().cleanupUnusedKeys(player);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onItemPickup(final EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof final Player player)) {
            return;
        }

        final AzoxStorage plugin = AzoxStorage.getInstance();
        plugin.getItemTransferLogger().logPickup(event.getEntity().getUniqueId(), event.getItem().getItemStack());
    }

    public static void activateBypass(final Player player) {
        BYPASS_ACTIVATED.add(player.getUniqueId());
    }

    public static void deactivateBypass(final Player player) {
        BYPASS_ACTIVATED.remove(player.getUniqueId());
    }

    public static boolean hasBypassActivated(final Player player) {
        return BYPASS_ACTIVATED.contains(player.getUniqueId());
    }
}
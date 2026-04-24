package net.azox.storage.listener;

import net.azox.storage.AzoxStorage;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public final class KeyListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(final PlayerInteractEvent event) {
        final Player player = event.getPlayer();
        final AzoxStorage plugin = AzoxStorage.getInstance();
        final var keyManager = plugin.getKeyManager();

        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        final ItemStack itemInHand = player.getInventory().getItemInMainHand();
        if (itemInHand == null || itemInHand.getType().isAir()) {
            return;
        }

        if (keyManager.isKeyItem(itemInHand)) {
            event.setCancelled(true);
            keyManager.digitizeKey(player, itemInHand);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onInventoryClose(final org.bukkit.event.inventory.InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof final Player player)) {
            return;
        }

        final AzoxStorage plugin = AzoxStorage.getInstance();
        final var keyManager = plugin.getKeyManager();
        keyManager.cleanupUnusedKeys(player);
    }
}
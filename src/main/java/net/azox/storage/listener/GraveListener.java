package net.azox.storage.listener;

import net.azox.storage.AzoxStorage;
import net.azox.storage.grave.GraveManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public final class GraveListener implements Listener {

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerInteract(final PlayerInteractEvent event) {
        final Player player = event.getPlayer();
        
        if (!player.isSneaking()) {
            return;
        }
        
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        
        final var block = event.getClickedBlock();
        if (block == null) {
            return;
        }
        
        final AzoxStorage plugin = AzoxStorage.getInstance();
        final var graveManager = plugin.getGraveManager();
        
        if (graveManager.isGrave(block.getLocation())) {
            event.setCancelled(true);
            graveManager.quickLoot(player, block.getLocation());
        }
    }
}

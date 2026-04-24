package net.azox.storage.listener;

import lombok.Getter;
import lombok.Setter;
import net.azox.storage.AzoxStorage;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
public final class DropItemListener implements Listener {

    private final AzoxStorage plugin;
    private final Map<UUID, DroppedItemData> droppedItems;

    public DropItemListener() {
        this.plugin = AzoxStorage.getInstance();
        this.droppedItems = new HashMap<>();
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerDropItem(final PlayerDropItemEvent event) {
        final Player player = event.getPlayer();
        final var item = event.getItemDrop();
        final var itemStack = item.getItemStack();

        final var data = new DroppedItemData(
                player.getUniqueId(),
                item.getUniqueId(),
                itemStack,
                System.currentTimeMillis(),
                player.getUniqueId()
        );

        this.droppedItems.put(item.getUniqueId(), data);

        this.plugin.getItemTransferLogger().logDrop(player.getUniqueId(), itemStack);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onItemDespawn(final ItemDespawnEvent event) {
        final var entity = event.getEntity();
        final var data = this.droppedItems.remove(entity.getUniqueId());

        if (data == null) {
            return;
        }

        final var lastAccessor = data.getLastAccessor();
        this.plugin.getGraveManager().markItemDespawned(data.getItem(), lastAccessor);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onItemPickup(final org.bukkit.event.entity.EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof final Player player)) {
            return;
        }

        final var item = event.getItem();
        final var data = this.droppedItems.get(item.getUniqueId());

        if (data == null) {
            return;
        }

        data.setLastAccessor(player.getUniqueId());
    }

    public void updateDespawnTimer(final org.bukkit.entity.Item item) {
        final var data = this.droppedItems.get(item.getUniqueId());
        if (data == null) {
            return;
        }

        final var age = item.getTicksLived();
        final var remaining = 6000 - age;

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!item.isValid()) {
                    this.cancel();
                    return;
                }

                final var loc = item.getLocation();
                final var world = loc.getWorld();
            }
        }.runTaskTimer(this.plugin, 0L, 20L);
    }

    @Getter
    @Setter
    public static class DroppedItemData {
        private UUID owner;
        private UUID itemId;
        private ItemStack item;
        private long droppedAt;
        private UUID lastAccessor;

        public DroppedItemData(final UUID owner, final UUID itemId, final ItemStack item, final long droppedAt, final UUID lastAccessor) {
            this.owner = owner;
            this.itemId = itemId;
            this.item = item;
            this.droppedAt = droppedAt;
            this.lastAccessor = lastAccessor;
        }

        public ItemStack getItem() {
            return this.item;
        }

        public UUID getLastAccessor() {
            return this.lastAccessor;
        }

        public void setLastAccessor(final UUID lastAccessor) {
            this.lastAccessor = lastAccessor;
        }
    }
}

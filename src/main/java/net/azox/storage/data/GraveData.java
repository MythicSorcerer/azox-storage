package net.azox.storage.data;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

@Getter
@Setter
public final class GraveData {

    private UUID id;
    private UUID ownerId;
    private String ownerName;
    private Location location;
    private ItemStack[] inventory;
    private ItemStack[] armor;
    private ItemStack skull;
    private ItemStack chest;
    private UUID armorStand;
    private boolean looted;
    private long createdAt;

    public GraveData(final UUID ownerId, final String ownerName, final Location location,
                    final ItemStack[] inventory, final ItemStack[] armor,
                    final ItemStack skull, final ItemStack chest) {
        this.id = UUID.randomUUID();
        this.ownerId = ownerId;
        this.ownerName = ownerName;
        this.location = location;
        this.inventory = inventory;
        this.armor = armor;
        this.skull = skull;
        this.chest = chest;
        this.looted = false;
        this.createdAt = System.currentTimeMillis();
    }

    public boolean isOwner(final UUID playerId) {
        return this.ownerId.equals(playerId);
    }
}

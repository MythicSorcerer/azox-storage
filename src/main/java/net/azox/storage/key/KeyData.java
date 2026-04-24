package net.azox.storage.key;

import lombok.Getter;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

@Getter
public final class KeyData {

    private final UUID owner;
    private final String ownerName;
    private final String containerKey;
    private final String keyId;
    private final KeyType type;
    private ItemStack physicalItem;

    public KeyData(final UUID owner, final String ownerName, final String containerKey,
                 final String keyId, final KeyType type) {
        this.owner = owner;
        this.ownerName = ownerName;
        this.containerKey = containerKey;
        this.keyId = keyId;
        this.type = type;
    }

    public UUID getOwner() {
        return this.owner;
    }

    public String getOwnerName() {
        return this.ownerName;
    }

    public String getContainerKey() {
        return this.containerKey;
    }

    public String getKeyId() {
        return this.keyId;
    }

    public KeyType getType() {
        return this.type;
    }

    public ItemStack getPhysicalItem() {
        return this.physicalItem;
    }

    public void setPhysicalItem(final ItemStack item) {
        this.physicalItem = item;
    }
}

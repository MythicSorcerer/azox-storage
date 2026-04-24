package net.azox.storage.container;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.Material;

import java.util.Set;
import java.util.UUID;

@Getter
@Setter
public final class ContainerData {

    private UUID owner;
    private String ownerName;
    private Material type;
    private String containerId;
    private Location location;
    private boolean locked;
    private Set<String> presets;

    public ContainerData(final UUID owner, final String ownerName, final Material type,
                       final String containerId, final Location location, final boolean locked) {
        this.owner = owner;
        this.ownerName = ownerName;
        this.type = type;
        this.containerId = containerId;
        this.location = location;
        this.locked = locked;
        this.presets = new java.util.HashSet<>();
    }

    public String getUniqueKey() {
        return this.owner.toString() + "_" + this.type.name() + "_" + this.containerId;
    }

    public void addPreset(final String preset) {
        this.presets.add(preset);
    }

    public void removePreset(final String preset) {
        this.presets.remove(preset);
    }

    public boolean hasPresetAccess(final UUID playerId) {
        return this.presets.contains(playerId.toString());
    }
}

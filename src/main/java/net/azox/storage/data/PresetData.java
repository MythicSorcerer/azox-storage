package net.azox.storage.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public final class PresetData {

    private String name;
    private UUID ownerId;
    private Set<UUID> players;
    private long createdAt;

    public boolean hasPlayer(final UUID playerId) {
        return this.players != null && this.players.contains(playerId);
    }

    public boolean addPlayer(final UUID playerId) {
        if (this.players == null) {
            this.players = new java.util.HashSet<>();
        }
        return this.players.add(playerId);
    }

    public boolean removePlayer(final UUID playerId) {
        return this.players != null && this.players.remove(playerId);
    }
}
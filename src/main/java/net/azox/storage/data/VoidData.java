package net.azox.storage.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bukkit.Location;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public final class VoidData {

    private UUID playerId;
    private List<String> items;
    private long timestamp;

    private static final class DespawnedItem {

        private UUID itemId;
        private String itemStackJson;
        private UUID lastAccessor;
        private long droppedAt;
        private Location location;
    }
}
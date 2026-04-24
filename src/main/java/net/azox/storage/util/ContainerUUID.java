package net.azox.storage.util;

import net.azox.storage.AzoxStorage;
import org.bukkit.Material;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ContainerUUID {

    private static final String ALPHANUMERIC = "0123456789abcdefghijklmnopqrstuvwxyz";
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Map<Material, String> TYPE_NAMES = Map.of(
            Material.CHEST, "Chest",
            Material.BARREL, "Barrel",
            Material.TRAPPED_CHEST, "Trapped Chest",
            Material.ENDER_CHEST, "Ender Chest"
    );

    public static String generate(UUID owner, Material containerType) {
        AzoxStorage plugin = AzoxStorage.getInstance();
        int length = plugin.getPluginConfig().getContainerUuidLength();

        String id;
        do {
            id = generateId(length);
        } while (isTaken(owner, containerType, id));

        return id;
    }

    private static String generateId(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(ALPHANUMERIC.charAt(RANDOM.nextInt(ALPHANUMERIC.length())));
        }
        return sb.toString();
    }

    private static boolean isTaken(UUID owner, Material containerType, String id) {
        return AzoxStorage.getInstance().getContainerManager()
                .getContainer(owner, containerType, id) != null;
    }

    public static String formatDisplayName(String playerName, Material containerType, String id) {
        String typeName = TYPE_NAMES.getOrDefault(containerType, "Container");
        return playerName + " " + typeName + " <" + id + ">";
    }

    public static boolean validateId(String id) {
        if (id == null || id.isEmpty()) {
            return false;
        }
        for (char c : id.toCharArray()) {
            if (ALPHANUMERIC.indexOf(c) == -1) {
                return false;
            }
        }
        return true;
    }

    public static List<String> getAllAlphanumeric() {
        return new ArrayList<>(List.of(ALPHANUMERIC.split("")));
    }
}
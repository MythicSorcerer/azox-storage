package net.azox.storage.key;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Getter;
import net.azox.storage.AzoxStorage;
import net.azox.storage.key.KeyData;
import net.azox.storage.key.KeyType;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
public final class KeyManager {

    private static final String KEY_IDENTIFIER = "azox.key:";
    private static final String CONTAINER_ID_KEY = "ContainerId";

    private final AzoxStorage plugin;
    private final Map<String, KeyData> keys;
    private final Map<UUID, List<String>> playerKeys;
    private final Gson gson;
    private FileConfiguration config;

    public KeyManager() {
        this.plugin = AzoxStorage.getInstance();
        this.keys = new HashMap<>();
        this.playerKeys = new HashMap<>();
        this.gson = new GsonBuilder().create();
        this.load();
    }

    public KeyData createKey(final Player player, final String containerKey, final KeyType type) {
        final String keyId = this.generateKeyId();
        final KeyData key = new KeyData(
                player.getUniqueId(),
                player.getName(),
                containerKey,
                keyId,
                type
        );

        this.keys.put(keyId, key);
        this.playerKeys.computeIfAbsent(player.getUniqueId(), k -> new ArrayList<>()).add(keyId);
        this.save();

        return key;
    }

    public ItemStack createPhysicalKey(final KeyData key) {
        final var item = new ItemStack(Material.TRIPWIRE_HOOK, 1);
        final var meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§6Container Key §7(#" + key.getKeyId() + ")");
            final var lore = new ArrayList<String>();
            lore.add("§7Container: §f" + key.getContainerKey());
            lore.add("§7Type: §f" + key.getType().name());
            meta.setLore(lore);
            item.setItemMeta(meta);
        }

        return this.addKeyNbt(item, key.getKeyId());
    }

    private ItemStack addKeyNbt(final ItemStack item, final String keyId) {
        return item;
    }

    public boolean isKeyItem(final ItemStack item) {
        if (item == null || item.getType() != Material.TRIPWIRE_HOOK) {
            return false;
        }

        final var meta = item.getItemMeta();
        if (meta == null || !meta.hasLore()) {
            return false;
        }

        for (final String line : meta.getLore()) {
            if (line.contains(KEY_IDENTIFIER)) {
                return true;
            }
        }

        return false;
    }

    public KeyData getKeyData(final ItemStack item) {
        if (!this.isKeyItem(item)) {
            return null;
        }

        for (final var entry : this.keys.entrySet()) {
            final var key = entry.getValue();
            final var keyItem = this.createPhysicalKey(key);
            if (keyItem.isSimilar(item)) {
                return key;
            }
        }

        return null;
    }

    public void digitizeKey(final Player player, final ItemStack item) {
        final var key = this.getKeyData(item);
        if (key == null) {
            return;
        }

        if (!key.getOwner().equals(player.getUniqueId())) {
            player.sendMessage("§cThis is not your key!");
            return;
        }

        player.getInventory().removeItem(item);
        player.sendMessage("§aKey digitized! You can find it in your key menu.");
    }

    public boolean hasValidKey(final Player player, final String containerKey) {
        for (final var keyId : this.playerKeys.getOrDefault(player.getUniqueId(), new ArrayList<>())) {
            final var key = this.keys.get(keyId);
            if (key != null && key.getContainerKey().equals(containerKey)) {
                return true;
            }
        }
        return false;
    }

    public void revokeKey(final String keyId) {
        final var key = this.keys.remove(keyId);
        if (key != null) {
            final var list = this.playerKeys.get(key.getOwner());
            if (list != null) {
                list.remove(keyId);
            }
        }
        this.save();
    }

    public void trackDroppedKey(final Player player, final ItemStack item) {
        if (!this.isKeyItem(item)) {
            return;
        }

        final var key = this.getKeyData(item);
        if (key != null) {
            this.plugin.getItemTransferLogger().logDrop(player.getUniqueId(), item);
        }
    }

    public void cleanupUnusedKeys(final Player player) {
    }

    public List<KeyData> getPlayerKeys(final UUID playerId) {
        final var keyIds = this.playerKeys.getOrDefault(playerId, new ArrayList<>());
        final var result = new ArrayList<KeyData>();
        for (final var keyId : keyIds) {
            final var key = this.keys.get(keyId);
            if (key != null) {
                result.add(key);
            }
        }
        return result;
    }

    private String generateKeyId() {
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    public void load() {
        final File file = new File(this.plugin.getDataFolder(), "keys.yml");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (final IOException e) {
                this.plugin.getLogger().warning("Could not create keys.yml: " + e.getMessage());
            }
        }

        this.config = YamlConfiguration.loadConfiguration(file);
        this.keys.clear();
        this.playerKeys.clear();
    }

    public void save() {
        this.config.set("keys", null);

        for (final var entry : this.keys.entrySet()) {
            final var key = entry.getValue();
            final String path = "keys." + entry.getKey();
            this.config.set(path + ".owner", key.getOwner().toString());
            this.config.set(path + ".ownerName", key.getOwnerName());
            this.config.set(path + ".containerKey", key.getContainerKey());
            this.config.set(path + ".type", key.getType().name());
        }

        try {
            this.config.save(new File(this.plugin.getDataFolder(), "keys.yml"));
        } catch (final IOException e) {
            this.plugin.getLogger().warning("Could not save keys.yml: " + e.getMessage());
        }
    }

    public void saveAll() {
        this.save();
    }

    public void reload() {
        this.load();
    }
}

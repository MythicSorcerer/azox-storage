package net.azox.storage.hook;

import net.azox.storage.AzoxStorage;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public final class TrialChambersHook {

    private final AzoxStorage plugin;
    private boolean isTrialChambersLoaded;

    public TrialChambersHook() {
        this.plugin = AzoxStorage.getInstance();
        this.checkTrialChambers();
    }

    private void checkTrialChambers() {
        final Plugin trialChambers = this.plugin.getServer().getPluginManager().getPlugin("TrialChambers");
        this.isTrialChambersLoaded = trialChambers != null;
    }

    public boolean isInTrialChamber(final Player player) {
        if (!this.isTrialChambersLoaded) {
            return false;
        }

        if (player.getWorld().getEnvironment() != org.bukkit.World.Environment.NORMAL) {
            return false;
        }

        final String worldName = player.getWorld().getName();
        return worldName.contains("trial") || worldName.contains("chamber");
    }

    public boolean isKeyUsageAllowed(final Player player) {
        if (!this.isInTrialChamber(player)) {
            return true;
        }

        final AzoxStorage plugin = AzoxStorage.getInstance();
        if (!plugin.getPluginConfig().isBlockKeyInTrialChambers()) {
            return true;
        }

        final double reach = plugin.getPluginConfig().getTrialChambersReach();
        if (reach >= 0) {
            return false;
        }

        return player.getGameMode() != org.bukkit.GameMode.ADVENTURE;
    }

    public void applyTrialChamberRestrictions(final Player player) {
        if (!this.isInTrialChamber(player)) {
            return;
        }

        final AzoxStorage plugin = AzoxStorage.getInstance();
        if (!plugin.getPluginConfig().isBlockKeyInTrialChambers()) {
            return;
        }

        final double reach = plugin.getPluginConfig().getTrialChambersReach();
        if (reach >= 0) {
            this.applyReachLimit(player, reach);
        }
    }

    private void applyReachLimit(final Player player, final double reach) {
        player.setMetadata("trial_chamber_reach", 
            new org.bukkit.metadata.FixedMetadataValue(this.plugin, reach));
    }

    public boolean isTrialChambersLoaded() {
        return this.isTrialChambersLoaded;
    }
}
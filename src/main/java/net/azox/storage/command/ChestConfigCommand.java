package net.azox.storage.command;

import net.azox.storage.AzoxStorage;
import net.azox.storage.container.ContainerData;
import net.azox.storage.util.MessageUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class ChestConfigCommand extends AbstractCommand {

    public ChestConfigCommand() {
        super("chestconfig", "azox.storage.use", "Open chest configuration menu");
    }

    @Override
    public boolean execute(final CommandSender sender, final String[] args) {
        if (!this.testPermission(sender)) {
            return true;
        }

        if (sender instanceof final Player player) {
            final AzoxStorage plugin = AzoxStorage.getInstance();
            final var container = plugin.getContainerManager().getContainer(player.getLocation());

            if (container != null) {
                this.openContainerConfig(player, container);
                return true;
            }
        }

        sender.sendMessage("§6Looking at a chest to configure it, or specify a container ID.");
        sender.sendMessage("§7Usage: /chestconfig [page]");
        return true;
    }

    private void openContainerConfig(final Player player, final ContainerData container) {
        if (!container.getOwner().equals(player.getUniqueId()) && 
                !player.hasPermission("azox.storage.admin")) {
            player.sendMessage("§cYou don't own this container!");
            return;
        }

        player.sendMessage("§6Configuring container: §e" + container.getContainerId());
        player.sendMessage("§7Owner: §f" + container.getOwnerName());
        player.sendMessage("§7Type: §f" + container.getType().name());
        player.sendMessage("§7Locked: §f" + (container.isLocked() ? "§aYes" : "§cNo"));
    }
}

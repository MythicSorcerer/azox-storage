package net.azox.storage.command;

import net.azox.storage.AzoxStorage;
import net.azox.storage.listener.PlayerListener;
import net.azox.storage.util.MessageUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class BypassLocksCommand extends AbstractCommand {

    public BypassLocksCommand() {
        super("bypasslocks", "azox.storage.bypass", "Toggle lock bypass mode");
    }

    @Override
    public boolean execute(final CommandSender sender, final String[] args) {
        if (!this.testPermission(sender)) {
            return true;
        }

        if (!(sender instanceof final Player player)) {
            sender.sendMessage("§cThis command can only be used by players!");
            return true;
        }

        if (PlayerListener.hasBypassActivated(player)) {
            PlayerListener.deactivateBypass(player);
            MessageUtil.send(player, "§cBypass deactivated!");
        } else {
            PlayerListener.activateBypass(player);
            MessageUtil.send(player, "§aBypass activated!");
        }

        return true;
    }
}

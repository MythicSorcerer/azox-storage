package net.azox.storage.command;

import net.azox.storage.AzoxStorage;
import net.azox.storage.util.MessageUtil;
import org.bukkit.command.CommandSender;

public final class AdminCommand extends AbstractCommand {

    public AdminCommand() {
        super("azoxstorage", "azox.storage.admin", "Main admin command");
    }

    @Override
    public boolean execute(final CommandSender sender, final String[] args) {
        if (!this.testPermission(sender)) {
            return true;
        }

        if (args.length == 0) {
            this.sendUsage(sender);
            return true;
        }

        final String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "reload" -> {
                final AzoxStorage plugin = AzoxStorage.getInstance();
                plugin.reloadPlugin();
                MessageUtil.send(sender, "§aConfiguration reloaded!");
            }
            case "info" -> {
                MessageUtil.send(sender, "§6Azox Storage §7v" + AzoxStorage.getInstance().getDescription().getVersion());
                MessageUtil.send(sender, "§7Author: §fAzox");
            }
            default -> this.sendUsage(sender);
        }

        return true;
    }

    private void sendUsage(final CommandSender sender) {
        sender.sendMessage("§6Azox Storage Admin Commands:");
        sender.sendMessage("§7/azoxstorage reload §f- Reload configuration");
        sender.sendMessage("§7/azoxstorage info §f- Plugin info");
    }
}

package net.azox.storage.command;

import lombok.Getter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

@Getter
public abstract class AbstractCommand extends Command implements CommandExecutor {

    private final String permission;
    private final String description;

    protected AbstractCommand(final String name, final String permission, final String description) {
        super(name);
        this.permission = permission;
        this.description = description;
        this.setPermission(permission);
        this.setUsage("/" + name);
    }

    @Override
    public boolean execute(final CommandSender sender, final String commandLabel, final String[] args) {
        if (!this.testPermission(sender)) {
            sender.sendMessage("§cYou don't have permission to use this command!");
            return true;
        }
        return this.execute(sender, args);
    }

    @Override
    public boolean onCommand(final CommandSender sender, final org.bukkit.command.Command command, final String label, final String[] args) {
        return this.execute(sender, args);
    }

    public abstract boolean execute(CommandSender sender, String[] args);
}

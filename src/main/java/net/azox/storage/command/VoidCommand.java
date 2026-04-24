package net.azox.storage.command;

import net.azox.storage.AzoxStorage;
import net.azox.storage.util.MessageUtil;
import org.bukkit.command.CommandSender;

public final class VoidCommand extends AbstractCommand {

    public VoidCommand() {
        super("void", "azox.storage.use", "Access void storage for lost items");
    }

    @Override
    public boolean execute(final CommandSender sender, final String[] args) {
        if (!this.testPermission(sender)) {
            return true;
        }

        final AzoxStorage plugin = AzoxStorage.getInstance();
        plugin.getGraveManager().openVoidInventory(sender);
        return true;
    }
}
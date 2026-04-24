package net.azox.storage.util;

import net.azox.storage.AzoxStorage;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class MessageUtil {

    private MessageUtil() {
    }

    public static void send(final CommandSender sender, final String message) {
        final AzoxStorage plugin = AzoxStorage.getInstance();
        final String prefix = plugin.getPluginConfig().getPrefix();
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + message));
    }

    public static void send(final Player player, final String message) {
        final AzoxStorage plugin = AzoxStorage.getInstance();
        final String prefix = plugin.getPluginConfig().getPrefix();
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + message));
    }

    public static void sendRaw(final CommandSender sender, final String message) {
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
    }

    public static void sendRaw(final Player player, final String message) {
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
    }

    public static void sendActionBar(final Player player, final String message) {
        player.spigot().sendMessage(
            ChatMessageType.ACTION_BAR,
            new TextComponent(ChatColor.translateAlternateColorCodes('&', message))
        );
    }

    public static void sendHover(final Player player, final String text, final String hover) {
        player.spigot().sendMessage(
            ChatMessageType.CHAT,
            new TextComponent(ChatColor.translateAlternateColorCodes('&', text))
        );
    }

    public static String color(final String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
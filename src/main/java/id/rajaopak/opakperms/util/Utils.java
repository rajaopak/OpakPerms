package id.rajaopak.opakperms.util;

import id.rajaopak.opakperms.OpakPerms;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

    public static final Pattern HEX_PATTERN = Pattern.compile("&#(\\w{5}[0-9a-f])");

    public static String colors(String string) {

        Matcher matcher = HEX_PATTERN.matcher(string);
        StringBuilder buffer = new StringBuilder();

        while (matcher.find()) {
            matcher.appendReplacement(buffer, ChatColor.of("#" + matcher.group(1)).toString());
        }

        return ChatColor.translateAlternateColorCodes('&', matcher.appendTail(buffer).toString());
    }

    public static void logDebug(String message) {
        if (OpakPerms.isDebug()) {
            Bukkit.getLogger().info(colors(message));
        }
    }

    public static String getPrefix() {
        return colors("&e&lOpak &a&l/ &r");
    }

    public static void sendMessage(CommandSender sender, String msg) {
        sender.sendMessage(colors(msg));
    }

    public static void sendMessageWithPrefix(CommandSender sender, String msg) {
        sender.sendMessage(getPrefix() + colors(msg));
    }

}

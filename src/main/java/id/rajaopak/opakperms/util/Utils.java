package id.rajaopak.opakperms.util;

import id.rajaopak.opakperms.OpakPerms;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.kyori.adventure.text.Component.space;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.*;
import static net.kyori.adventure.text.format.TextDecoration.BOLD;

public class Utils {

    public static final Pattern HEX_PATTERN = Pattern.compile("&#(\\w{5}[0-9a-f])");

    private final OpakPerms core;

    public Utils(OpakPerms core) {
        this.core = core;
    }

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
            Bukkit.getLogger().info(colors("[OpakPerms] " + message));
        }
    }

    public static Component getPrefix() {
        // colors("&e&lOpak &a&l/ &r")
        return text()
                .append(text()
                        .color(DARK_GRAY)
                        .append(text('['))
                        .append(text()
                                .decoration(BOLD, true)
                                .append(text('O', YELLOW))
                                .append(text('P', GOLD))
                        )
                        .append(text(']')))
                .append(space())
                .build();
    }

    public static TextComponent prefixed(ComponentLike component) {
        return text()
                .append(text()
                        .color(DARK_GRAY)
                        .append(text('['))
                        .append(text()
                                .decoration(BOLD, true)
                                .append(text('O', YELLOW))
                                .append(text('P', GOLD))
                        )
                        .append(text(']')))
                .append(space())
                .append(component)
                .build();
    }

    public static void sendMessage(CommandSender sender, String msg) {
        sender.sendMessage(colors(msg));
    }

    public static void sendMessageWithPrefix(CommandSender sender, String msg) {
        sender.sendMessage(colors(LegacyComponentSerializer.legacySection().serialize(getPrefix()) + msg));
    }

    public void sendMessage(CommandSender sender, Component component) {
        this.core.getAudiences().sender(sender).sendMessage(component);
    }

    public void sendMessageWithPrefix(CommandSender sender, Component component) {
        this.core.getAudiences().sender(sender).sendMessage(text()
                .append(getPrefix())
                .append(space())
                .append(component)
                .build());
    }
}

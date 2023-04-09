package id.rajaopak.opakperms.commands;

import cloud.commandframework.CommandHelpHandler;
import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandDescription;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import cloud.commandframework.annotations.specifier.Greedy;
import cloud.commandframework.annotations.suggestions.Suggestions;
import cloud.commandframework.context.CommandContext;
import id.rajaopak.opakperms.OpakPerms;
import id.rajaopak.opakperms.util.Utils;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;
import redis.clients.jedis.Jedis;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MainCommand {

    private final OpakPerms core;

    public MainCommand(OpakPerms core) {
        this.core = core;
    }

    @CommandMethod("opakperms|operms|oop")
    @CommandDescription("Help menu")
    @CommandPermission("opakperms.help")
    public void commandHelp(final @NonNull CommandSender sender) {
        core.getMinecraftHelp().queryCommands("1", sender);
    }

    @CommandMethod("opakperms|operms|oop help [query]")
    @CommandDescription("Help menu")
    @CommandPermission("opakperms.help")
    public void commandHelp(
            final @NonNull CommandSender sender,
            final @Argument(value = "query", suggestions = "commandList") @Greedy String query) {
        core.getMinecraftHelp().queryCommands(query == null ? "" : query, sender);
    }

    @CommandMethod("opakperms|operms|oop redisstatus")
    @CommandPermission("opakperms.redis.status")
    public void redisStatus(final @NonNull CommandSender sender) {
        Utils.sendMessageWithPrefix(sender, "&eStatus: " + (this.core.getRedisManager().isRedisConnected()));
    }

    @CommandMethod("opakperms|operms|oop redisreconnect")
    @CommandPermission("opakperms.redis.reconnect")
    public void redisReconnect(final @NonNull CommandSender sender) {
        Utils.sendMessageWithPrefix(sender, "&aReconnecting redis connection");
        Utils.sendMessageWithPrefix(sender, "&eConnection: " + this.core.getRedisManager().connect(this.core.getConfig().getString("host"),
                this.core.getConfig().getInt("port"),
                this.core.getConfig().getString("password"),
                this.core.getConfig().getString("channel")));
        Utils.sendMessageWithPrefix(sender, "&eStatus: " + (this.core.getRedisManager().isRedisConnected()));
    }

    @CommandMethod("opakperms|operms|oop ping")
    @CommandPermission("opakperms.redis.ping")
    public void redisPing(final @NonNull CommandSender sender) {
        try (Jedis j = this.core.getRedisManager().getPublisherPool().getResource()) {
            Utils.sendMessageWithPrefix(sender, "&ePublisherPool: " + j.ping());
        }

        try (Jedis j = this.core.getRedisManager().getSubscriberPool().getResource()) {
            Utils.sendMessageWithPrefix(sender, "&eSubscriberPool: " + j.ping());
        }
    }

    @CommandMethod("opakperms|operms|oop reload")
    @CommandPermission("opakperms.reload")
    public void reload(final @NonNull CommandSender sender) {
        this.core.reloadConfig();
        this.core.reload();
        Utils.sendMessageWithPrefix(sender, "&aConfig reloaded!");
    }

    @Suggestions("help")
    public List<String> help(CommandContext<CommandSender> sender, String context) {
        return Stream.of("reload", "help", "redisstatus", "redisreconnect", "ping")
                .filter(s -> s.startsWith(context))
                .sorted().collect(Collectors.toList());
    }

    @Suggestions("commandList")
    public List<String> commandList(CommandContext<CommandSender> sender, String context) {
        return this.core.getManager().createCommandHelpHandler().queryRootIndex(sender.getSender()).getEntries().stream()
                .map(CommandHelpHandler.VerboseHelpEntry::getSyntaxString)
                .filter(s -> s.startsWith(context))
                .sorted()
                .collect(Collectors.toList());
    }
}

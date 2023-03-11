package id.rajaopak.opakperms.commands;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import cloud.commandframework.annotations.specifier.Greedy;
import id.rajaopak.opakperms.OpakPerms;
import id.rajaopak.opakperms.enums.LpActionType;
import id.rajaopak.opakperms.manager.CommandManager;
import id.rajaopak.opakperms.manager.NodeExtractor;
import id.rajaopak.opakperms.messager.UserUpdateMessageImpl;
import id.rajaopak.opakperms.util.Utils;
import net.luckperms.api.model.data.DataMutateResult;
import net.luckperms.api.node.ChatMetaType;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.PrefixNode;
import net.luckperms.api.query.QueryOptions;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Map;
import java.util.UUID;

public class PrefixCommand extends CommandManager {

    private final OpakPerms core;

    public PrefixCommand(OpakPerms core) {
        super(core);
        this.core = core;
    }

    @CommandMethod("addprefix <player> <priority> <prefix>")
    @CommandPermission("opakperms.addprefix")
    public void addPrefix(final @NonNull CommandSender sender,
                          final @NonNull @Argument(value = "player", defaultValue = "self", suggestions = "player") String targetName,
                          final @Argument(value = "priority") int priority,
                          final @NonNull @Greedy @Argument(value = "prefix") String prefix) {

        OfflinePlayer player = this.core.getServer().getOfflinePlayer(targetName);

        if (!player.hasPlayedBefore()) {
            Utils.sendMessageWithPrefix(sender, "&cPlayer not found!");
            return;
        }

        if (!prefix.startsWith("\"") && !prefix.endsWith("\"")) {
            Utils.sendMessageWithPrefix(sender, "&cPlease start the message with \" symbol and ends it with that symbol too.");
            return;
        }

        String pref = prefix.replace("\"", "");

        this.core.getLuckPerms().getUserManager().modifyUser(player.getUniqueId(), user -> {
            Node node = PrefixNode.builder(pref, priority).build();

            if (this.core.getRedisManager().sendRequest(new UserUpdateMessageImpl(UUID.randomUUID(), targetName, LpActionType.ADD, NodeExtractor.parseNode(node)).asEncodedString())) {
                Utils.sendMessageWithPrefix(sender, "&aSuccessfully add &e" + pref + " &aprefix to &b" + player.getName() + "&a.");
            } else {
                DataMutateResult result = user.data().add(node);

                if (result.wasSuccessful()) {
                    user.data().add(node);
                    Utils.sendMessageWithPrefix(sender, "&aSuccessfully add &e" + pref + " &aprefix to &b" + player.getName() + "&a.");
                } else {
                    Utils.sendMessageWithPrefix(sender, "&b" + player.getName() + " &calready have that prefix.");
                }
            }
        });
    }

    @CommandMethod("setprefix <player> <prefix>")
    @CommandPermission("opakperms.setprefix")
    public void setPrefix(final @NonNull CommandSender sender,
                          final @NonNull @Argument(value = "player", defaultValue = "self", suggestions = "player") String targetName,
                          final @NonNull @Greedy @Argument(value = "prefix") String prefix) {

        OfflinePlayer player = this.core.getServer().getOfflinePlayer(targetName);

        if (!player.hasPlayedBefore()) {
            Utils.sendMessageWithPrefix(sender, "&cPlayer not found!");
            return;
        }

        if (!prefix.startsWith("\"") && !prefix.endsWith("\"")) {
            Utils.sendMessageWithPrefix(sender, "&cPlease start the message with \" symbol and ends it with that symbol too.");
            return;
        }

        String pref = prefix.replace("\"", "");

        this.core.getLuckPerms().getUserManager().modifyUser(player.getUniqueId(), user -> {
            Map<Integer, String> inheritedPrefixes = user.getCachedData().getMetaData(QueryOptions.nonContextual()).getPrefixes();
            int priority = inheritedPrefixes.keySet().stream().mapToInt(i -> i + 10).max().orElse(10);

            Node node = PrefixNode.builder(pref, priority).build();

            if (this.core.getRedisManager().sendRequest(new UserUpdateMessageImpl(UUID.randomUUID(), targetName, LpActionType.SET, NodeExtractor.parseNode(node)).asEncodedString())) {
                Utils.sendMessageWithPrefix(sender, "&aSuccessfully set &e" + pref + " &aprefix to &b" + player + "&a.");
            } else {
                DataMutateResult result = user.data().add(node);

                if (result.wasSuccessful()) {
                    user.data().clear(NodeType.PREFIX::matches);
                    user.data().add(node);
                    Utils.sendMessageWithPrefix(sender, "&aSuccessfully set &e" + pref + " &aprefix to &b" + player + "&a.");
                } else {
                    Utils.sendMessageWithPrefix(sender, "&b" + player.getName() + " &calready have that prefix.");
                }
            }
        });
    }

    @CommandMethod("removeprefix <player> <priority>")
    @CommandPermission("opakperms.removeprefix")
    public void removePrefix(final @NonNull CommandSender sender,
                             final @NonNull @Argument(value = "player", defaultValue = "self", suggestions = "player") String targetName,
                             final @Argument(value = "priority") int priority) {

        OfflinePlayer player = this.core.getServer().getOfflinePlayer(targetName);

        if (!player.hasPlayedBefore()) {
            Utils.sendMessageWithPrefix(sender, "&cPlayer not found!");
            return;
        }

        this.core.getLuckPerms().getUserManager().modifyUser(player.getUniqueId(), user -> {
            Node node = ChatMetaType.PREFIX.builder().priority(priority).build();

            if (this.core.getRedisManager().sendRequest(new UserUpdateMessageImpl(UUID.randomUUID(), targetName, LpActionType.REMOVE, NodeExtractor.parseNode(node)).asEncodedString())) {
                Utils.sendMessageWithPrefix(sender, "&aSuccessfully remove all prefix with priority &e" + priority + " &afor &b" + user.getUsername() + "&a.");
            } else {
                DataMutateResult result = user.data().remove(node);

                if (result.wasSuccessful()) {
                    Utils.sendMessageWithPrefix(sender, "&aSuccessfully remove all prefix with priority &e" + priority + " &afor &b" + user.getUsername() + "&a.");
                } else {
                    Utils.sendMessageWithPrefix(sender, "&b" + player.getName() + " &cdoesn't have prefix with priority &e" + priority + "&c.");
                }
            }
        });
    }

    @CommandMethod("clearprefix <player>")
    @CommandPermission("opakperms.clearprefix")
    public void clearPrefix(final @NonNull CommandSender sender,
                            final @NonNull @Argument(value = "player", defaultValue = "self", suggestions = "player") String targetName) {

        OfflinePlayer player = this.core.getServer().getOfflinePlayer(targetName);

        if (!player.hasPlayedBefore()) {
            Utils.sendMessageWithPrefix(sender, "&cPlayer not found!");
            return;
        }

        Node node = ChatMetaType.PREFIX.builder().priority(0).build();

        this.core.getLuckPerms().getUserManager().modifyUser(player.getUniqueId(), user -> {
            if (this.core.getRedisManager().sendRequest(new UserUpdateMessageImpl(UUID.randomUUID(), targetName, LpActionType.CLEAR, NodeExtractor.parseNode(node)).asEncodedString())) {
                Utils.sendMessageWithPrefix(sender, "&aSuccessfully clear all prefix for &b" + player.getName() + "&a.");
            } else {
                user.data().clear(NodeType.PREFIX::matches);
                Utils.sendMessageWithPrefix(sender, "&aSuccessfully clear all prefix for &b" + player.getName() + "&a.");
            }
        });
    }
}

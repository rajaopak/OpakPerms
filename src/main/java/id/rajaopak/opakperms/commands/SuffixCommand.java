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
import net.luckperms.api.node.types.SuffixNode;
import net.luckperms.api.query.QueryOptions;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Map;
import java.util.UUID;

public class SuffixCommand extends CommandManager {

    private final OpakPerms core;

    public SuffixCommand(OpakPerms core) {
        super(core);
        this.core = core;
    }

    @CommandMethod("addsuffix <player> <priority> <suffix>")
    @CommandPermission("opakperms.addsuffix")
    public void addSuffix(final @NonNull CommandSender sender,
                          final @NonNull @Argument(value = "player", defaultValue = "self", suggestions = "player") String targetName,
                          final @Argument(value = "priority") int priority,
                          final @NonNull @Greedy @Argument(value = "suffix") String suffix) {

        UUID uuid = this.core.getLuckPerms().getUserManager().lookupUniqueId(targetName).join();

        if (uuid == null) {
            Utils.sendMessageWithPrefix(sender, "&cPlayer not found!");
            return;
        }

        OfflinePlayer player = this.core.getServer().getOfflinePlayer(uuid);

        if (!player.hasPlayedBefore()) {
            Utils.sendMessageWithPrefix(sender, "&cPlayer not found!");
            return;
        }

        this.core.getLuckPerms().getUserManager().modifyUser(player.getUniqueId(), user -> {
            Node node = SuffixNode.builder(suffix, priority).build();

            if (this.core.getRedisManager().sendRequest(new UserUpdateMessageImpl(UUID.randomUUID(), targetName, LpActionType.ADD, NodeExtractor.parseNode(node)).asEncodedString())) {
                Utils.sendMessageWithPrefix(sender, "&aSuccessfully add &e" + suffix + " &asuffix to &b" + player.getName() + "&a.");
            } else {
                DataMutateResult result = user.data().add(node);

                if (result.wasSuccessful()) {
                    Utils.sendMessageWithPrefix(sender, "&aSuccessfully add &e" + suffix + " &asuffix to &b" + player.getName() + "&a.");
                } else {
                    Utils.sendMessageWithPrefix(sender, "&b" + player.getName() + " &calready have that suffix.");
                }
            }
        });
    }

    @CommandMethod("setsuffix <player> <suffix>")
    @CommandPermission("opakperms.setsuffix")
    public void setSuffix(final @NonNull CommandSender sender,
                          final @NonNull @Argument(value = "player", defaultValue = "self", suggestions = "player") String targetName,
                          final @NonNull @Greedy @Argument(value = "suffix") String suffix) {

        UUID uuid = this.core.getLuckPerms().getUserManager().lookupUniqueId(targetName).join();

        if (uuid == null) {
            Utils.sendMessageWithPrefix(sender, "&cPlayer not found!");
            return;
        }

        OfflinePlayer player = this.core.getServer().getOfflinePlayer(uuid);

        if (!player.hasPlayedBefore()) {
            Utils.sendMessageWithPrefix(sender, "&cPlayer not found!");
            return;
        }

        this.core.getLuckPerms().getUserManager().modifyUser(player.getUniqueId(), user -> {
            Map<Integer, String> inheritedSuffixes = user.getCachedData().getMetaData(QueryOptions.nonContextual()).getSuffixes();
            int priority = inheritedSuffixes.keySet().stream().mapToInt(i -> i + 10).max().orElse(10);

            Node node = SuffixNode.builder(suffix, priority).build();

            if (this.core.getRedisManager().sendRequest(new UserUpdateMessageImpl(UUID.randomUUID(), targetName, LpActionType.SET, NodeExtractor.parseNode(node)).asEncodedString())) {
                Utils.sendMessageWithPrefix(sender, "&aSuccessfully set &e" + suffix + " &asuffix to &b" + player.getName() + "&a.");
            } else {
                DataMutateResult result = user.data().add(node);

                if (result.wasSuccessful()) {
                    user.data().clear(NodeType.SUFFIX::matches);
                    user.data().add(node);
                    Utils.sendMessageWithPrefix(sender, "&aSuccessfully set &e" + suffix + " &asuffix to &b" + player.getName() + "&a.");
                } else {
                    Utils.sendMessageWithPrefix(sender, "&b" + player.getName() + " &calready have that suffix.");
                }
            }
        });
    }

    @CommandMethod("removesuffix <player> <priority>")
    @CommandPermission("opakperms.removesuffix")
    public void removeSuffix(final @NonNull CommandSender sender,
                             final @NonNull @Argument(value = "player", defaultValue = "self", suggestions = "player") String targetName,
                             final @Argument(value = "priority") int priority) {

        UUID uuid = this.core.getLuckPerms().getUserManager().lookupUniqueId(targetName).join();

        if (uuid == null) {
            Utils.sendMessageWithPrefix(sender, "&cPlayer not found!");
            return;
        }

        OfflinePlayer player = this.core.getServer().getOfflinePlayer(uuid);

        if (!player.hasPlayedBefore()) {
            Utils.sendMessageWithPrefix(sender, "&cPlayer not found!");
            return;
        }

        this.core.getLuckPerms().getUserManager().modifyUser(player.getUniqueId(), user -> {
            Node node = ChatMetaType.SUFFIX.builder().priority(priority).build();

            if (this.core.getRedisManager().sendRequest(new UserUpdateMessageImpl(UUID.randomUUID(), targetName, LpActionType.REMOVE, NodeExtractor.parseNode(node)).asEncodedString())) {
                Utils.sendMessageWithPrefix(sender, "&aSuccessfully remove all suffix with priority &e" + priority + " &afor &b" + user.getUsername() + "&a.");
            } else {
                DataMutateResult result = user.data().remove(node);

                if (result.wasSuccessful()) {
                    Utils.sendMessageWithPrefix(sender, "&aSuccessfully remove all suffix with priority &e" + priority + " &afor &b" + user.getUsername() + "&a.");
                } else {
                    Utils.sendMessageWithPrefix(sender, "&b" + player.getName() + " &cdoesn't have that suffix.");
                }
            }
        });
    }

    @CommandMethod("clearsuffix <player>")
    @CommandPermission("opakperms.clearsuffix")
    public void clearSuffix(final @NonNull CommandSender sender,
                            final @NonNull @Argument(value = "player", defaultValue = "self", suggestions = "player") String targetName) {

        UUID uuid = this.core.getLuckPerms().getUserManager().lookupUniqueId(targetName).join();

        if (uuid == null) {
            Utils.sendMessageWithPrefix(sender, "&cPlayer not found!");
            return;
        }

        OfflinePlayer player = this.core.getServer().getOfflinePlayer(uuid);

        if (!player.hasPlayedBefore()) {
            Utils.sendMessageWithPrefix(sender, "&cPlayer not found!");
            return;
        }

        this.core.getLuckPerms().getUserManager().modifyUser(player.getUniqueId(), user -> {
            Node node = ChatMetaType.SUFFIX.builder().priority(0).build();

            if (this.core.getRedisManager().sendRequest(new UserUpdateMessageImpl(UUID.randomUUID(), targetName, LpActionType.CLEAR, NodeExtractor.parseNode(node)).asEncodedString())) {
                Utils.sendMessageWithPrefix(sender, "&aSuccessfully clear all suffix for &b" + user.getUsername() + "&a.");
            } else {
                user.data().clear(NodeType.SUFFIX::matches);
                Utils.sendMessageWithPrefix(sender, "&aSuccessfully clear all suffix for &b" + user.getUsername() + "&a.");
            }
        });
    }
}

package id.rajaopak.opakperms.commands;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import id.rajaopak.opakperms.OpakPerms;
import id.rajaopak.opakperms.enums.LpActionType;
import id.rajaopak.opakperms.manager.CommandManager;
import id.rajaopak.opakperms.manager.NodeExtractor;
import id.rajaopak.opakperms.messager.UserUpdateMessageImpl;
import id.rajaopak.opakperms.util.Utils;
import net.luckperms.api.model.data.DataMutateResult;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.InheritanceNode;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.UUID;

public class RankCommand extends CommandManager {

    private final OpakPerms core;

    public RankCommand(OpakPerms core) {
        super(core);
        this.core = core;
    }

    @CommandMethod("setrank <player> [rank]")
    @CommandPermission("opakperms.setrank")
    public void setRank(final @NonNull CommandSender sender,
                        final @NonNull @Argument(value = "player", suggestions = "player") String targetName,
                        final @NonNull @Argument(value = "rank", suggestions = "rank") String rank) {

        OfflinePlayer player = this.core.getServer().getOfflinePlayer(targetName);

        if (!player.hasPlayedBefore()) {
            Utils.sendMessage(sender, Utils.getPrefix() + "&cPlayer not found!");
            return;
        }

        if (this.core.getLuckPerms().getGroupManager().getGroup(rank) != null) {
            Group group = this.core.getLuckPerms().getGroupManager().getGroup(rank);
            Node node = InheritanceNode.builder(group).build();

            this.core.getLuckPerms().getUserManager().modifyUser(player.getUniqueId(), user -> {

                if (this.core.getRedisManager().sendRequest(new UserUpdateMessageImpl(UUID.randomUUID(), targetName, LpActionType.SET, NodeExtractor.parseNode(node)).asEncodedString())) {
                    Utils.sendMessageWithPrefix(sender, "&aSuccessfully set &e" + player.getName() + " &arank to &b" + group.getName() + "&a.");
                } else {
                    DataMutateResult result = user.data().add(node);

                    if (result.wasSuccessful()) {
                        user.data().clear(NodeType.INHERITANCE::matches);
                        user.data().add(node);
                        Utils.sendMessageWithPrefix(sender, "&aSuccessfully remove &e" + group.getName() + " &arank to &b" + player.getName() + "&a.");
                    } else {
                        Utils.sendMessageWithPrefix(sender, "&b" + player.getName() + " &calready have that rank.");
                    }
                }
            });
        } else {
            Utils.sendMessageWithPrefix(sender, "&cGroup with that name is not existed!");
        }
    }

    @CommandMethod("addrank <player> [rank]")
    @CommandPermission("opakperms.addrank")
    public void addRank(final @NonNull CommandSender sender,
                        final @NonNull @Argument(value = "player", suggestions = "player") String targetName,
                        final @NonNull @Argument(value = "rank", suggestions = "rank") String rank) {

        OfflinePlayer player = this.core.getServer().getOfflinePlayer(targetName);

        if (!player.hasPlayedBefore()) {
            Utils.sendMessage(sender, Utils.getPrefix() + "&cPlayer not found!");
            return;
        }

        if (this.core.getLuckPerms().getGroupManager().getGroup(rank) != null) {
            Group group = this.core.getLuckPerms().getGroupManager().getGroup(rank);

            this.core.getLuckPerms().getUserManager().modifyUser(player.getUniqueId(), user -> {
                Node node = InheritanceNode.builder(group).build();

                if (this.core.getRedisManager().sendRequest(new UserUpdateMessageImpl(UUID.randomUUID(), targetName, LpActionType.ADD, NodeExtractor.parseNode(node)).asEncodedString())) {
                    Utils.sendMessageWithPrefix(sender, "&aSuccessfully added &e" + player.getName() + " &arank to &b" + group.getName() + "&a.");
                } else {
                    DataMutateResult result = user.data().add(node);

                    if (result.wasSuccessful()) {
                        Utils.sendMessageWithPrefix(sender, "&aSuccessfully remove &e" + group.getName() + " &arank to &b" + player.getName() + "&a.");
                    } else {
                        Utils.sendMessageWithPrefix(sender, "&b" + player.getName() + " &calready have that rank.");
                    }
                }
            });
        } else {
            Utils.sendMessageWithPrefix(sender, "&cGroup with that name is not existed!");
        }
    }

    @CommandMethod("removerank <player> [rank]")
    @CommandPermission("opakperms.removerank")
    public void removeRank(final @NonNull CommandSender sender,
                           final @NonNull @Argument(value = "player", suggestions = "player") String targetName,
                           final @NonNull @Argument(value = "rank", suggestions = "rank") String rank) {

        OfflinePlayer player = this.core.getServer().getOfflinePlayer(targetName);

        if (!player.hasPlayedBefore()) {
            Utils.sendMessage(sender, Utils.getPrefix() + "&cPlayer not found!");
            return;
        }

        if (this.core.getLuckPerms().getGroupManager().getGroup(rank) != null) {
            Group group = this.core.getLuckPerms().getGroupManager().getGroup(rank);

            this.core.getLuckPerms().getUserManager().modifyUser(player.getUniqueId(), user -> {
                Node node = InheritanceNode.builder(group).build();

                if (this.core.getRedisManager().sendRequest(new UserUpdateMessageImpl(UUID.randomUUID(), targetName, LpActionType.REMOVE, NodeExtractor.parseNode(node)).asEncodedString())) {
                    Utils.sendMessageWithPrefix(sender, "&aSuccessfully remove &b" + group.getName() + " &arank to &e" + user.getUsername() + "&a.");
                } else {
                    DataMutateResult result = user.data().remove(node);

                    if (result.wasSuccessful()) {
                        Utils.sendMessageWithPrefix(sender, "&aSuccessfully remove &e" + group.getName() + " &arank to &b" + player.getName() + "&a.");
                    } else {
                        Utils.sendMessageWithPrefix(sender, "&b" + player.getName() + " &cdoesn't have that rank.");
                    }
                }
            });
        } else {
            Utils.sendMessageWithPrefix(sender, "&cGroup with that name is not existed!");
        }
    }

    @CommandMethod("clearrank <player>")
    @CommandPermission("opakperms.removerank")
    public void clearRank(final @NonNull CommandSender sender,
                          final @NonNull @Argument(value = "player", suggestions = "player") String targetName) {

        OfflinePlayer player = this.core.getServer().getOfflinePlayer(targetName);

        if (!player.hasPlayedBefore()) {
            Utils.sendMessage(sender, Utils.getPrefix() + "&cPlayer not found!");
            return;
        }

        this.core.getLuckPerms().getUserManager().modifyUser(player.getUniqueId(), user -> {
            if (this.core.getRedisManager().sendRequest(new UserUpdateMessageImpl(UUID.randomUUID(), targetName, LpActionType.CLEAR, NodeExtractor.parseNode(InheritanceNode.builder().group("tempGroup").build())).asEncodedString())) {
                Utils.sendMessageWithPrefix(sender, "&aSuccessfully clear all rank that &e" + user.getUsername() + "&a has.");
            } else {
                user.data().clear(NodeType.INHERITANCE::matches);
                Utils.sendMessageWithPrefix(sender, "&aSuccessfully clear all rank that &e" + user.getUsername() + "&a has.");
            }
        });
    }
}

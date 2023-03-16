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
import net.luckperms.api.node.Node;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.PermissionNode;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.UUID;

public class PermissionCommand extends CommandManager {

    private final OpakPerms core;

    public PermissionCommand(OpakPerms core) {
        super(core);
        this.core = core;
    }

    @CommandMethod("addpermission <player> <permission> <value>")
    @CommandPermission("opakperms.addpermission")
    public void addPermission(final @NonNull CommandSender sender,
                              final @NonNull @Argument(value = "player", defaultValue = "self", suggestions = "player") String targetName,
                              final @NonNull @Argument(value = "permission", suggestions = "permission") String permission,
                              final @Nullable @Argument(value = "value", defaultValue = "true") String value) {

        OfflinePlayer player = this.core.getServer().getOfflinePlayer(targetName);

        if (!player.hasPlayedBefore()) {
            Utils.sendMessageWithPrefix(sender, "&cPlayer not found!");
            return;
        }

        String v;

        if (value == null) {
            v = "true";
        } else {
            if (value.equalsIgnoreCase("false")) {
                v = "false";
            } else {
                Utils.sendMessageWithPrefix(sender, "&cPlease select between true/false.");
                return;
            }
        }

        Node node;
        if (v.equalsIgnoreCase("true")) {
            node = PermissionNode.builder(permission).build();
        } else {
            node = PermissionNode.builder(permission).value(false).build();
        }

        this.core.getLuckPerms().getUserManager().modifyUser(player.getUniqueId(), user -> {
            if (this.core.getRedisManager().sendRequest(new UserUpdateMessageImpl(UUID.randomUUID(), player.getName(), LpActionType.ADD, NodeExtractor.parseNode(node)).asEncodedString())) {
                Utils.sendMessageWithPrefix(sender, "&aSuccessfully set &e" + permission + " &apermission to &b" + player.getName() + "&a.");
            } else {
                DataMutateResult result = user.data().add(node);

                if (result.wasSuccessful()) {
                    user.data().add(node);
                    Utils.sendMessageWithPrefix(sender, "&aSuccessfully set &e" + permission + " &apermission to &b" + player.getName() + "&a.");
                } else {
                    Utils.sendMessageWithPrefix(sender, "&b" + player.getName() + " &calready has permission &e" + permission + "&c.");
                }
            }
        });
    }

    @CommandMethod("removepermission <player> <permission>")
    @CommandPermission("opakperms.removepermission")
    public void removePermission(final @NonNull CommandSender sender,
                                 final @NonNull @Argument(value = "player", defaultValue = "self", suggestions = "player") String targetName,
                                 final @NonNull @Argument(value = "permission", suggestions = "playerPermission") String permission) {

        OfflinePlayer player = this.core.getServer().getOfflinePlayer(targetName);

        if (!player.hasPlayedBefore()) {
            Utils.sendMessageWithPrefix(sender, "&cPlayer not found!");
            return;
        }

        Node node = Node.builder(permission).build();

        this.core.getLuckPerms().getUserManager().modifyUser(player.getUniqueId(), user -> {

            if (this.core.getRedisManager().sendRequest(new UserUpdateMessageImpl(UUID.randomUUID(), player.getName(), LpActionType.REMOVE, NodeExtractor.parseNode(node)).asEncodedString())) {
                Utils.sendMessageWithPrefix(sender, "&aSuccessfully remove &e" + permission + " &apermission from &b" + player.getName() + "&a.");
            } else {
                DataMutateResult result = user.data().remove(node);

                if (result.wasSuccessful()) {
                    user.data().remove(node);
                    Utils.sendMessageWithPrefix(sender, "&aSuccessfully set &e" + permission + " &apermission from &b" + player.getName() + "&a.");
                } else {
                    Utils.sendMessageWithPrefix(sender, "&b" + player.getName() + " &calready doesn't have permission &e" + permission + "&c.");
                }
            }
        });
    }

    @CommandMethod("removepermission <player>")
    @CommandPermission("opakperms.clearpermission")
    public void clearPermission(final @NonNull CommandSender sender,
                                final @NonNull @Argument(value = "player", defaultValue = "self", suggestions = "player") String targetName) {

        OfflinePlayer player = this.core.getServer().getOfflinePlayer(targetName);

        if (!player.hasPlayedBefore()) {
            Utils.sendMessageWithPrefix(sender, "&cPlayer not found!");
            return;
        }

        Node node = Node.builder("opakperms.temp.perm").build();

        this.core.getLuckPerms().getUserManager().modifyUser(player.getUniqueId(), user -> {
            if (this.core.getRedisManager().sendRequest(new UserUpdateMessageImpl(UUID.randomUUID(), player.getName(), LpActionType.CLEAR, NodeExtractor.parseNode(node)).asEncodedString())) {
                Utils.sendMessageWithPrefix(sender, "&aSuccessfully clear all permission from &b" + player.getName() + "&a.");
            } else {
                user.data().clear(NodeType.PERMISSION::matches);
                Utils.sendMessageWithPrefix(sender, "&aSuccessfully clear all permission from &b" + player.getName() + "&a.");
            }
        });
    }
}

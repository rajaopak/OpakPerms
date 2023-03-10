package id.rajaopak.opakperms.commands;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import id.rajaopak.opakperms.OpakPerms;
import id.rajaopak.opakperms.manager.CommandManager;
import id.rajaopak.opakperms.util.Utils;
import net.luckperms.api.model.data.DataMutateResult;
import net.luckperms.api.node.Node;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.time.Duration;

public class AddPermission extends CommandManager {

    private final OpakPerms core;

    public AddPermission(OpakPerms core) {
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
        Duration duration = null;
        if (v.equalsIgnoreCase("true")) {
            node = Node.builder(permission).build();
        } else {
            node = Node.builder(permission).value(false).build();
        }

        this.core.getLuckPerms().getUserManager().modifyUser(player.getUniqueId(), user -> {
            DataMutateResult result = user.data().add(node);

            if (result.wasSuccessful()) {
                // ToDo: send message
            } else {
                // ToDo: send message
            }
        });
    }

    @CommandMethod("removepermission <player> [permission]")
    @CommandPermission("opakperms.removepermission")
    public void removePermission(final @NonNull CommandSender sender,
                              final @NonNull @Argument(value = "player", defaultValue = "self", suggestions = "player") String targetName,
                              final @NonNull @Argument(value = "permission") String permission) {


        OfflinePlayer player = this.core.getServer().getOfflinePlayer(targetName);

        if (!player.hasPlayedBefore()) {
            Utils.sendMessageWithPrefix(sender, "&cPlayer not found!");
            return;
        }

        Node node = Node.builder(permission).build();

        this.core.getLuckPerms().getUserManager().modifyUser(player.getUniqueId(), user -> {
            DataMutateResult result = user.data().remove(node);

            if (result.wasSuccessful()) {
                // ToDo: send message
            } else {
                // ToDo: send message
            }
        });
    }
}

package id.rajaopak.opakperms.commands;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import id.rajaopak.opakperms.OpakPerms;
import id.rajaopak.opakperms.manager.CommandManager;
import id.rajaopak.opakperms.util.Utils;
import net.luckperms.api.actionlog.Action;
import net.luckperms.api.model.data.DataMutateResult;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.InheritanceNode;
import net.luckperms.api.query.QueryOptions;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.concurrent.ExecutionException;

public class SetRank extends CommandManager {

    private final OpakPerms core;

    public SetRank(OpakPerms core) {
        super(core);
        this.core = core;
    }

    @CommandMethod("setrank <player> [rank]")
    @CommandPermission("opakperms.setrank")
    public void setPlayerRank(final @NonNull CommandSender sender,
                              final @NonNull @Argument(value = "player", defaultValue = "self", suggestions = "player") String targetName,
                              final @NonNull @Argument(value = "rank", suggestions = "rank") String rank) {

        OfflinePlayer player = this.core.getServer().getOfflinePlayer(targetName);

        if (!player.hasPlayedBefore()) {
            Utils.sendMsg(sender, Utils.getPrefix() + "&cPlayer not found!");
            return;
        }

        if (this.core.getLuckPerms().getGroupManager().getGroup(rank) != null) {
            Group group = this.core.getLuckPerms().getGroupManager().getGroup(rank);

            this.core.getLuckPerms().getUserManager().modifyUser(player.getUniqueId(), user -> {
                user.data().clear(NodeType.INHERITANCE::matches);

                Node node = InheritanceNode.builder(group).build();

                user.data().add(node);

                // ToDo: send message
            });
        } else {
            Utils.sendMsg(sender, Utils.getPrefix() + "&cGroup with that name is not existed!");
        }
    }

    @CommandMethod("addrank <player> [rank]")
    @CommandPermission("opakperms.addrank")
    public void addPlayerRank(final @NonNull CommandSender sender,
                              final @NonNull @Argument(value = "player", defaultValue = "self", suggestions = "player") String targetName,
                              final @NonNull @Argument(value = "rank", suggestions = "rank") String rank) {

        OfflinePlayer player = this.core.getServer().getOfflinePlayer(targetName);

        if (!player.hasPlayedBefore()) {
            Utils.sendMsg(sender, Utils.getPrefix() + "&cPlayer not found!");
            return;
        }

        if (this.core.getLuckPerms().getGroupManager().getGroup(rank) != null) {
            Group group = this.core.getLuckPerms().getGroupManager().getGroup(rank);

            this.core.getLuckPerms().getUserManager().modifyUser(player.getUniqueId(), user -> {
                Node node = InheritanceNode.builder(group).build();

                DataMutateResult result = user.data().add(node);
                if (result.wasSuccessful()) {

                }

                // ToDo: send message
            });
        } else {
            Utils.sendMsg(sender, Utils.getPrefix() + "&cGroup with that name is not existed!");
        }
    }
}

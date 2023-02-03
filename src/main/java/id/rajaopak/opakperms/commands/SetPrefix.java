package id.rajaopak.opakperms.commands;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import cloud.commandframework.annotations.specifier.Greedy;
import id.rajaopak.opakperms.OpakPerms;
import id.rajaopak.opakperms.manager.CommandManager;
import id.rajaopak.opakperms.util.Utils;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.types.PrefixNode;
import net.luckperms.api.query.QueryOptions;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Map;

public class SetPrefix extends CommandManager {

    private final OpakPerms core;

    public SetPrefix(OpakPerms core) {
        super(core);
        this.core = core;
    }

    @CommandMethod("setprefix <player> [prefix]")
    @CommandPermission("opakperms.setprefix")
    public void addPermission(final @NonNull CommandSender sender,
                              final @NonNull @Argument(value = "player", defaultValue = "self", suggestions = "player") String targetName,
                              final @NonNull @Greedy @Argument(value = "prefix") String prefix) {

        OfflinePlayer player = this.core.getServer().getOfflinePlayer(targetName);

        if (!player.hasPlayedBefore()) {
            Utils.sendMsg(sender, Utils.getPrefix() + "&cPlayer not found!");
            return;
        }

        if (!prefix.startsWith("\"") && !prefix.endsWith("\"")) {
            Utils.sendMsg(sender, Utils.getPrefix() + "&cPlease start the message with \" symbol and ends it with that symbol too.");
            return;
        }

        String pref = prefix.replace("\"", "");

        this.core.getLuckPerms().getUserManager().modifyUser(player.getUniqueId(), user -> {
            // Find the highest priority of their other prefixes
            // We need to do this because they might inherit a prefix from a parent group,
            // and we want the prefix we set to override that!
            Map<Integer, String> inheritedPrefixes = user.getCachedData().getMetaData(QueryOptions.nonContextual()).getPrefixes();
            int priority = inheritedPrefixes.keySet().stream().mapToInt(i -> i + 10).max().orElse(10);

            // Create a node to add to the player.
            Node node = PrefixNode.builder(pref, priority).build();

            // Add the node to the user.
            user.data().add(node);

            // ToDo: send message
        });
    }
}

package id.rajaopak.opakperms.commands;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandMethod;
import com.google.common.collect.Maps;
import id.rajaopak.opakperms.OpakPerms;
import id.rajaopak.opakperms.manager.CommandManager;
import id.rajaopak.opakperms.util.DurationFormatter;
import id.rajaopak.opakperms.util.UUIDChecker;
import id.rajaopak.opakperms.util.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.luckperms.api.context.Context;
import net.luckperms.api.context.ContextSet;
import net.luckperms.api.context.ImmutableContextSet;
import net.luckperms.api.event.player.lookup.UniqueIdDetermineTypeEvent;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.InheritanceNode;
import net.luckperms.api.query.QueryOptions;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.*;
import java.util.stream.Collectors;

import static id.rajaopak.opakperms.util.Utils.prefixed;
import static net.kyori.adventure.text.Component.*;
import static net.kyori.adventure.text.JoinConfiguration.newlines;
import static net.kyori.adventure.text.format.NamedTextColor.*;
import static net.kyori.adventure.text.format.Style.style;
import static net.kyori.adventure.text.format.TextDecoration.BOLD;

public class InfoCommand extends CommandManager {

    private final OpakPerms core;

    private final TextComponent OPEN_BRACKET = Component.text('(');
    private final TextComponent CLOSE_BRACKET = Component.text(')');
    Args4<String, String, Boolean, Component> SEND_USER_INFO_GENERAL = (username, uuid, status, component) -> join(newlines(),
            prefixed(text()
                    .color(YELLOW)
                    .append(text(">", style(BOLD))).append(space())
                    .append(text("User info: "))
                    .append(text(username, WHITE))),
            prefixed(text()
                    .color(GOLD)
                    .append(text("-", style(GRAY))).append(space())
                    .append(text("UUID: "))
                    .append(text(uuid, WHITE))),
            prefixed(text()
                    .color(GRAY)
                    .append(text("    "))
                    .append(OPEN_BRACKET)
                    .append(translatable("type"))
                    .append(text(": "))
                    .append(component)
                    .append(CLOSE_BRACKET)),
            prefixed(text()
                    .color(GOLD)
                    .append(text("-", style(GRAY))).append(space())
                    .append(text("Status: ")))
                    .append(status ? text("Online", GREEN) : text("Offline", RED))
    );
    Args0 PARENT_HEADER = () -> prefixed(text()
            .color(GREEN)
            .append(text("-", style(GRAY))).append(space())
            .append(text("Parent Groups: ")));
    Args0 TEMP_PARENT_HEADER = () -> prefixed(text()
            .color(GREEN)
            .append(text("-", style(GRAY))).append(space())
            .append(text("Temporary Parent Groups: ")));
    Args1<InheritanceNode> PARENT_NODE_ENTRY = node -> prefixed(text()
            .append(text("    >", GOLD))
            .append(space())
            .append(text(node.getGroupName(), WHITE))
            .append(space())
            .append(formatContextSetBracketed(node.getContexts(), empty()))
    );
    Args1<InheritanceNode> TEMP_PARENT_NODE_ENTRY = node -> prefixed(text()
            .append(text("    >", GOLD))
            .append(space())
            .append(text(node.getGroupName(), WHITE))
            .append(space())
            .append(formatContextSetBracketed(node.getContexts(), empty()))
            .append(DurationFormatter.LONG.format(node.getExpiryDuration()))
    );
    Args7<User, Boolean, ImmutableContextSet, String, String, String, Map<String, List<String>>> SEND_USER_INFO_CONTEXTUAL = (user, active, contexts, prefix, suffix, primaryGroup, meta) -> join(JoinConfiguration.separator(newline()),
            prefixed(text()
                    .color(GREEN)
                    .append(text("-", style(GRAY))).append(space())
                    .append(text("Contextual Data: "))
                    .append(OPEN_BRACKET.color(DARK_GRAY))
                    .append(text("mode: ", WHITE))
                    .append(active ? text("active player", DARK_GREEN) : text("server", GRAY)))
                    .append(CLOSE_BRACKET.color(DARK_GRAY)),
            prefixed(text()
                    .color(GOLD)
                    .append(text("    "))
                    .append(text("Contexts: "))
                    .append(formatContextSetBracketed(contexts, text("None", YELLOW)))),
            prefixed(text()
                    .color(GOLD)
                    .append(text("    "))
                    .append(text("Prefix: "))
                    .apply(builder -> {
                        if (user.getCachedData().getMetaData().getPrefix() == null) {
                            builder.append(text("None", YELLOW));
                        } else {
                            builder.append(text()
                                    .color(WHITE)
                                    .append(text('"'))
                                    .append(formatColoredValue(user.getCachedData().getMetaData().getPrefix()))
                                    .append(text('"')));
                        }
                    })),
            prefixed(text()
                    .color(GOLD)
                    .append(text("    "))
                    .append(text("Suffix: "))
                    .apply(builder -> {
                        if (user.getCachedData().getMetaData().getSuffix() == null) {
                            builder.append(text("None", YELLOW));
                        } else {
                            builder.append(text()
                                    .color(WHITE)
                                    .append(text('"'))
                                    .append(formatColoredValue(user.getCachedData().getMetaData().getSuffix()))
                                    .append(text('"')));
                        }
                    })),
            prefixed(text()
                    .color(GOLD)
                    .append(text("    "))
                    .append(text("Primary Group: "))
                    .append(text(user.getPrimaryGroup(), WHITE))),
            prefixed(text()
                    .color(GOLD)
                    .append(text("    "))
                    .append(translatable("Meta: "))
                    .apply(builder -> {
                        if (meta.isEmpty()) {
                            builder.append(text("None", YELLOW));
                        } else {
                            List<Component> entries = meta.entrySet().stream()
                                    .flatMap(entry -> entry.getValue().stream().map(value -> Maps.immutableEntry(entry.getKey(), value)))
                                    .map(entry -> text()
                                            .color(DARK_GRAY)
                                            .append(OPEN_BRACKET)
                                            .append(text(entry.getKey(), GRAY))
                                            .append(text('=', GRAY))
                                            .append(text().color(WHITE).append(formatColoredValue(entry.getValue())))
                                            .append(CLOSE_BRACKET)
                                            .build()
                                    )
                                    .collect(Collectors.toList());
                            builder.append(join(JoinConfiguration.separator(space()), entries));
                        }
                    }))
    );

    public InfoCommand(OpakPerms core) {
        super(core);
        this.core = core;
    }

    @CommandMethod("userinfo <player>")
    public void userinfo(final @NonNull CommandSender sender,
                         final @NonNull @Argument(value = "player", suggestions = "player") String targetName) {

        UUID uuid = this.core.getLuckPerms().getUserManager().lookupUniqueId(targetName).join();

        if (uuid == null) {
            Utils.sendMessageWithPrefix(sender, "&cPlayer not found!");
            return;
        }

        User user = this.core.getLuckPerms().getUserManager().loadUser(uuid).join();

        if (user == null) {
            Utils.sendMessageWithPrefix(sender, "&cPlayer not found!");
            return;
        }

        Map<Boolean, List<InheritanceNode>> parents = user.getNodes().stream()
                .filter(NodeType.INHERITANCE::matches)
                .map(NodeType.INHERITANCE::cast)
                .filter(Node::getValue)
                .collect(Collectors.groupingBy(Node::hasExpiry, Collectors.toList()));

        List<InheritanceNode> temporaryParents = parents.getOrDefault(true, Collections.emptyList());
        List<InheritanceNode> permanentParents = parents.getOrDefault(false, Collections.emptyList());

        Component uuidType = UUIDChecker.determineType(uuid);

        SEND_USER_INFO_GENERAL.send(sender, user.getFriendlyName(), user.getUniqueId().toString(), Bukkit.getOfflinePlayer(uuid).isOnline(), uuidType);

        if (!permanentParents.isEmpty()) {
            PARENT_HEADER.send(sender);
            for (InheritanceNode node : permanentParents) {
                PARENT_NODE_ENTRY.send(sender, node);
            }
        }

        if (!temporaryParents.isEmpty()) {
            TEMP_PARENT_HEADER.send(sender);
            for (InheritanceNode node : temporaryParents) {
                TEMP_PARENT_NODE_ENTRY.send(sender, node);
            }
        }

        QueryOptions queryOptions = this.core.getLuckPerms().getContextManager().getQueryOptions(user).orElse(null);
        boolean active = true;

        if (queryOptions == null) {
            active = false;
            queryOptions = this.core.getLuckPerms().getContextManager().getStaticQueryOptions();
        }

        Map<String, List<String>> meta = user.getCachedData().getMetaData().getMeta();

        SEND_USER_INFO_CONTEXTUAL.send(sender, user, active, queryOptions.context(), user.getCachedData().getMetaData().getPrefix(), user.getCachedData().getMetaData().getSuffix(), user.getPrimaryGroup(), meta);
    }

    private Component formatContextSetBracketed(ContextSet set, Component ifEmpty) {
        // "&8(&7server=&fsurvival&8) &8(&7world=&fnether&8)"
        Iterator<Context> it = set.iterator();
        if (!it.hasNext()) {
            return ifEmpty;
        }

        TextComponent.Builder builder = text();

        builder.append(formatContextBracketed(it.next()));
        while (it.hasNext()) {
            builder.append(text(" "));
            builder.append(formatContextBracketed(it.next()));
        }

        return builder.build();
    }

    private Component formatContextBracketed(String key, String value) {
        // &8(&7{}=&f{}&8)
        return text()
                .color(DARK_GRAY)
                .append(OPEN_BRACKET)
                .append(text(key, GRAY))
                .append(text('=', GRAY))
                .append(text(value, WHITE))
                .append(CLOSE_BRACKET)
                .build();
    }

    private Component formatContextBracketed(Context context) {
        return formatContextBracketed(context.getKey(), context.getValue());
    }

    private Component formatColoredValue(String value) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(value).toBuilder()
                .hoverEvent(HoverEvent.showText(text(value, WHITE))).build();
    }

    interface Args0 {
        Component build();

        default void send(CommandSender sender) {
            OpakPerms.getInstance().getUtils().sendMessage(sender, build());
        }
    }

    interface Args1<A0> {
        Component build(A0 args0);

        default void send(CommandSender sender, A0 args0) {
            OpakPerms.getInstance().getUtils().sendMessage(sender, build(args0));
        }
    }

    interface Args4<A0, A1, A2, A3> {
        Component build(A0 arg0, A1 arg1, A2 arg2, A3 arg3);

        default void send(CommandSender sender, A0 arg0, A1 arg1, A2 arg2, A3 arg3) {
            OpakPerms.getInstance().getUtils().sendMessage(sender, build(arg0, arg1, arg2, arg3));
        }
    }

    interface Args7<A0, A1, A2, A3, A4, A5, A6> {
        Component build(A0 arg0, A1 arg1, A2 arg2, A3 arg3, A4 arg4, A5 arg5, A6 arg6);

        default void send(CommandSender sender, A0 arg0, A1 arg1, A2 arg2, A3 arg3, A4 arg4, A5 arg5, A6 arg6) {
            OpakPerms.getInstance().getUtils().sendMessage(sender, build(arg0, arg1, arg2, arg3, arg4, arg5, arg6));
        }
    }
}
package id.rajaopak.opakperms.manager;

import cloud.commandframework.annotations.suggestions.Suggestions;
import cloud.commandframework.context.CommandContext;
import id.rajaopak.opakperms.OpakPerms;
import id.rajaopak.opakperms.callback.CanSkipCallback;
import id.rajaopak.opakperms.util.Utils;
import net.luckperms.api.model.group.Group;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class CommandManager {
    private final OpakPerms core;

    public CommandManager(OpakPerms core) {
        this.core = core;
    }

    protected TargetsCallback getTargets(CommandSender sender, @Nullable String arg) {
        TargetsCallback callback = new TargetsCallback();
        if (sender instanceof Player) {
            if (arg == null) {
                callback.add((Player) sender);
                return callback;
            }

            switch (arg.toLowerCase()) {
                case "self" -> {
                    callback.add((Player) sender);
                    return callback;
                }
                case "*", "@a", "@all" -> {
                    callback.addAll(Bukkit.getOnlinePlayers());
                    return callback;
                }
            }

            Player targetName = Bukkit.getPlayer(arg);
            if (targetName == null) {
                Utils.sendMsg(sender, Utils.getPrefix() + "&aNo player founded!");
                return callback;
            }

            callback.add(targetName);
            return callback;
        }

        if (arg == null) {
            Utils.sendMsg(sender, Utils.getPrefix() + "&cPlease add specific player!");
            callback.setNotify(true);
            return callback;
        }

        switch (arg.toLowerCase()) {
            case "*", "@a", "@all" -> {
                callback.addAll(Bukkit.getOnlinePlayers());
                return callback;
            }
        }

        Player targetName = Bukkit.getPlayer(arg);
        if (targetName == null) {
            Utils.sendMsg(sender, Utils.getPrefix() + "&aNo player founded!");
            return callback;
        }

        callback.add(targetName);
        return callback;
    }

    @SuppressWarnings("deprecation")
    protected OfflineTargetsCallback getOfflineTargets(CommandSender sender, @Nullable String arg) {
        OfflineTargetsCallback callback = new OfflineTargetsCallback();
        if (sender instanceof Player) {
            if (arg == null) {
                callback.add((Player) sender);
                return callback;
            }

            switch (arg.toLowerCase()) {
                case "self" -> {
                    callback.add((Player) sender);
                    return callback;
                }
                case "*", "@a", "@all" -> {
                    callback.addAll(Bukkit.getOnlinePlayers());
                    return callback;
                }
            }

            OfflinePlayer targetName = Bukkit.getOfflinePlayer(arg);
            if (!targetName.getPlayer().hasPlayedBefore()) {
                Utils.sendMsg(sender, Utils.getPrefix() + "&aNo player founded!");
                return callback;
            }

            callback.add(targetName);
            return callback;
        }

        if (arg == null) {
            Utils.sendMsg(sender, Utils.getPrefix() + "&cPlease add specific player!");
            callback.setNotify(true);
            return callback;
        }

        switch (arg.toLowerCase()) {
            case "*", "@a", "@all" -> {
                callback.addAll(Bukkit.getOnlinePlayers());
                return callback;
            }
        }

        OfflinePlayer targetName = Bukkit.getOfflinePlayer(arg);
        if (!targetName.getPlayer().hasPlayedBefore()) {
            Utils.sendMsg(sender, Utils.getPrefix() + "&aNo player founded!");
            return callback;
        }

        callback.add(targetName);
        return callback;
    }

    public CanSkipCallback canSkip(String action, TargetsCallback targetsCallback, CommandSender sender) {
        if (!core.getConfig().getBoolean("use-confirmation")) {
            return new CanSkipCallback(sender, true, null);
        }

        if (targetsCallback.size() == 1) {
            Player target = targetsCallback.getTargets().stream().findFirst().orElse(null);
            if (target != null && target.equals(sender)) {
                return new CanSkipCallback(sender, true, null);
            }
        }

        if (targetsCallback.size() >= 2) {
            return new CanSkipCallback(sender, false, Collections.singletonList(
                    Utils.colors(Utils.getPrefix() + "&7Are you sure want to execute &e" + action + " &7on &a" + targetsCallback.size() + " &7players?")
            ));
        }

        boolean playerSender = sender instanceof Player;

        World world = null;
        for (Player target : targetsCallback.getTargets()) {
            if (world != null) {
                if (world != target.getWorld()) {
                    return new CanSkipCallback(sender, false, Arrays.asList(
                            Utils.colors(Utils.getPrefix() + "&7Are you sure want to execute &e" + action + " &7on &a" + targetsCallback.size() + " &7players?"),
                            Utils.colors(Utils.getPrefix() + "&7Some player are scattered across different world.")
                    ));
                }

                if (playerSender) {
                    if (((Player) sender).getWorld() == target.getWorld() && ((Player) sender).getLocation().distanceSquared(target.getLocation()) >= 250) {
                        return new CanSkipCallback(sender, false, Arrays.asList(
                                Utils.colors(Utils.getPrefix() + "&7Are you sure want to execute &e" + action + " &7on &a" + targetsCallback.size() + " &7players?"),
                                Utils.colors(Utils.getPrefix() + "&7Some player are really far from you.")
                        ));
                    }
                }
                continue;
            }
            world = target.getWorld();
        }

        return new CanSkipCallback(sender, true, null);
    }

    @Suggestions("players")
    public List<String> players(CommandContext<CommandSender> sender, String context) {
        return Bukkit.getOnlinePlayers().stream().map(Player::getName).sorted().collect(Collectors.toCollection(() -> List.of("*", "@a", "@all")));
    }

    @Suggestions("player")
    public List<String> player(CommandContext<CommandSender> sender, String context) {
        return Bukkit.getOnlinePlayers().stream().map(Player::getName).sorted().collect(Collectors.toList());
    }

    @Suggestions("toggles")
    public List<String> toggle(CommandContext<CommandSender> sender, String context) {
        return Stream.of("on", "off", "toggle").filter(s -> s.toLowerCase().startsWith(context.toLowerCase())).collect(Collectors.toList());
    }

    @Suggestions("rank")
    public List<String> rank(final @NonNull CommandContext<CommandSender> context, final @NonNull String input) {
        return core.getLuckPerms().getGroupManager().getLoadedGroups().stream().map(Group::getName).collect(Collectors.toList());
    }

    @Suggestions("permission")
    public List<String> permission(final @NonNull CommandContext<CommandSender> context, final @NonNull String input) {
        return List.of("No Suggestion for now");
    }

    protected static class TargetsCallback {
        private boolean notify = false;
        private Set<Player> targets = new HashSet<>();

        public TargetsCallback() {
        }

        public void add(Player player) {
            this.targets.add(player);
        }

        public void addAll(Collection<? extends Player> players) {
            this.targets.addAll(players);
        }

        public int size() {
            return this.targets.size();
        }

        public boolean isEmpty() {
            return this.targets.isEmpty();
        }

        public boolean notifyIfEmpty() {
            return this.isEmpty() && !this.isNotify();
        }

        public boolean doesNotContain(Player player) {
            return !this.targets.contains(player);
        }

        public Stream<Player> stream() {
            return StreamSupport.stream(Spliterators.spliterator(targets, 0), false);
        }

        public void forEach(Consumer<? super Player> action) {
            for (Player target : targets) {
                action.accept(target);
            }
        }

        public boolean isNotify() {
            return this.notify;
        }

        public Set<Player> getTargets() {
            return this.targets;
        }

        public void setNotify(boolean notify) {
            this.notify = notify;
        }

        public void setTargets(Set<Player> targets) {
            this.targets = targets;
        }

        public boolean equals(final Object o) {
            if (o == this) return true;
            if (!(o instanceof final TargetsCallback other)) return false;
            if (!other.canEqual(this)) return false;
            if (this.isNotify() != other.isNotify()) return false;
            final Object this$targets = this.getTargets();
            final Object other$targets = other.getTargets();
            return Objects.equals(this$targets, other$targets);
        }

        protected boolean canEqual(final Object other) {
            return other instanceof TargetsCallback;
        }

        public int hashCode() {
            final int PRIME = 59;
            int result = 1;
            result = result * PRIME + (this.isNotify() ? 79 : 97);
            final Object $targets = this.getTargets();
            result = result * PRIME + ($targets == null ? 43 : $targets.hashCode());
            return result;
        }

        public String toString() {
            return "CommandManager.TargetsCallback(notify=" + this.isNotify() + ", targets=" + this.getTargets() + ")";
        }
    }

    protected static class OfflineTargetsCallback {
        private boolean notify = false;
        private Set<OfflinePlayer> targets = new HashSet<>();

        public OfflineTargetsCallback() {
        }

        public void add(OfflinePlayer player) {
            this.targets.add(player);
        }

        public void addAll(Collection<? extends OfflinePlayer> players) {
            this.targets.addAll(players);
        }

        public int size() {
            return this.targets.size();
        }

        public boolean isEmpty() {
            return this.targets.isEmpty();
        }

        public boolean notifyIfEmpty() {
            return this.isEmpty() && !this.isNotify();
        }

        public boolean doesNotContain(Player player) {
            return !this.targets.contains(player);
        }

        public Stream<OfflinePlayer> stream() {
            return StreamSupport.stream(Spliterators.spliterator(targets, 0), false);
        }

        public void forEach(Consumer<? super OfflinePlayer> action) {
            for (OfflinePlayer target : targets) {
                action.accept(target);
            }
        }

        public boolean isNotify() {
            return this.notify;
        }

        public Set<OfflinePlayer> getTargets() {
            return this.targets;
        }

        public void setNotify(boolean notify) {
            this.notify = notify;
        }

        public void setTargets(Set<OfflinePlayer> targets) {
            this.targets = targets;
        }

        public boolean equals(final Object o) {
            if (o == this) return true;
            if (!(o instanceof final OfflineTargetsCallback other)) return false;
            if (!other.canEqual(this)) return false;
            if (this.isNotify() != other.isNotify()) return false;
            final Object this$targets = this.getTargets();
            final Object other$targets = other.getTargets();
            return Objects.equals(this$targets, other$targets);
        }

        protected boolean canEqual(final Object other) {
            return other instanceof OfflineTargetsCallback;
        }

        public int hashCode() {
            final int PRIME = 59;
            int result = 1;
            result = result * PRIME + (this.isNotify() ? 79 : 97);
            final Object $targets = this.getTargets();
            result = result * PRIME + ($targets == null ? 43 : $targets.hashCode());
            return result;
        }

        public String toString() {
            return "CommandManager.OfflineTargetsCallback(notify=" + this.isNotify() + ", targets=" + this.getTargets() + ")";
        }
    }
}

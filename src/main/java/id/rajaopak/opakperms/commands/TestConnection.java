package id.rajaopak.opakperms.commands;

import cloud.commandframework.annotations.CommandMethod;
import id.rajaopak.opakperms.OpakPerms;
import id.rajaopak.opakperms.util.Utils;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;

public class TestConnection {

    private final OpakPerms core;

    public TestConnection(OpakPerms core) {
        this.core = core;
    }

    @CommandMethod("testredis")
    public void testRedisConnection(final @NonNull CommandSender sender) {
        Utils.sendMsg(sender, "&eStatus: " + (this.core.getRedisManager().isRedisConnected()));
    }

    @CommandMethod("reconnectredis")
    public void reconnectRedis(final @NonNull CommandSender sender) {
        Utils.sendMsg(sender, "&aReconnecting redis connection");
        Utils.sendMsg(sender, "&eConnection: " + this.core.getRedisManager().connect(this.core.getConfig().getString("host"),
                this.core.getConfig().getInt("port"),
                this.core.getConfig().getString("password"),
                this.core.getConfig().getString("channel")));
        Utils.sendMsg(sender, "&eStatus: " + (this.core.getRedisManager().isRedisConnected()));
    }
}

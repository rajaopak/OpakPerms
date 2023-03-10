package id.rajaopak.opakperms.commands;

import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import id.rajaopak.opakperms.OpakPerms;
import id.rajaopak.opakperms.util.Utils;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;
import redis.clients.jedis.Jedis;

public class TestConnection {

    private final OpakPerms core;

    public TestConnection(OpakPerms core) {
        this.core = core;
    }

    @CommandMethod("opakperms redisstatus")
    @CommandPermission("opakperms.redis.status")
    public void redisStatus(final @NonNull CommandSender sender) {
        Utils.sendMessageWithPrefix(sender, "&eStatus: " + (this.core.getRedisManager().isRedisConnected()));
    }

    @CommandMethod("opakperms redisreconnect")
    @CommandPermission("opakperms.redis.reconnect")
    public void redisReconnect(final @NonNull CommandSender sender) {
        Utils.sendMessageWithPrefix(sender, "&aReconnecting redis connection");
        Utils.sendMessageWithPrefix(sender, "&eConnection: " + this.core.getRedisManager().connect(this.core.getConfig().getString("host"),
                this.core.getConfig().getInt("port"),
                this.core.getConfig().getString("password"),
                this.core.getConfig().getString("channel")));
        Utils.sendMessageWithPrefix(sender, "&eStatus: " + (this.core.getRedisManager().isRedisConnected()));
    }

    @CommandMethod("opakperms ping")
    @CommandPermission("opakperms.redis.ping")
    public void redisPing(final @NonNull CommandSender sender) {
        try (Jedis j = this.core.getRedisManager().getPublisherPool().getResource()) {
            Utils.sendMessageWithPrefix(sender, "&ePublisherPool: " + j.ping());
        }

        try (Jedis j = this.core.getRedisManager().getSubscriberPool().getResource()) {
            Utils.sendMessageWithPrefix(sender, "&eSubscriberPool: " + j.ping());
        }
    }
}

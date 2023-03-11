package id.rajaopak.opakperms.commands;

import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import id.rajaopak.opakperms.OpakPerms;
import id.rajaopak.opakperms.util.Utils;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;

public class ReloadConfig {

    private final OpakPerms core;

    public ReloadConfig(OpakPerms core) {
        this.core = core;
    }

    @CommandMethod("opakreload")
    @CommandPermission("opakperms.reload")
    public void testRedisConnection(final @NonNull CommandSender sender) {
        this.core.reloadConfig();
        this.core.reload();
        Utils.sendMessageWithPrefix(sender, "&aConfig reloaded!");
    }

}

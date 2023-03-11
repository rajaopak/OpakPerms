package id.rajaopak.opakperms.callback;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record CanSkipCallback(CommandSender sender, boolean canSkip, @Nullable List<String> reason) {
}


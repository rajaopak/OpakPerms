package id.rajaopak.opakperms.callback;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public record CanSkipCallback(CommandSender sender, boolean canSkip, @Nullable List<String> reason) {
}


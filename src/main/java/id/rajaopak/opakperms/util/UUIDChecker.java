package id.rajaopak.opakperms.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.luckperms.api.event.player.lookup.UniqueIdDetermineTypeEvent;

import java.util.UUID;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.DARK_GRAY;
import static net.kyori.adventure.text.format.NamedTextColor.GREEN;

public class UUIDChecker {
    public static Component determineType(UUID uniqueId) {
        // determine initial type based on the uuid version
        String type = switch (uniqueId.version()) {
            case 4 -> UniqueIdDetermineTypeEvent.TYPE_AUTHENTICATED;
            case 3 -> UniqueIdDetermineTypeEvent.TYPE_UNAUTHENTICATED;
            case 2 -> UniqueIdDetermineTypeEvent.TYPE_NPC;
            default -> UniqueIdDetermineTypeEvent.TYPE_UNKNOWN;
        };

        return switch (type) {
            case UniqueIdDetermineTypeEvent.TYPE_AUTHENTICATED -> text("official").color(GREEN).hoverEvent(HoverEvent.showText(text("official").color(GREEN)));
            case UniqueIdDetermineTypeEvent.TYPE_UNAUTHENTICATED -> text("offline").color(DARK_GRAY).hoverEvent(HoverEvent.showText(text("offline").color(DARK_GRAY)));
            case UniqueIdDetermineTypeEvent.TYPE_NPC -> text("npc").color(DARK_GRAY).hoverEvent(HoverEvent.showText(text("npc").color(DARK_GRAY)));
            case UniqueIdDetermineTypeEvent.TYPE_UNKNOWN -> text("unknown").color(DARK_GRAY).hoverEvent(HoverEvent.showText(text("unknown").color(DARK_GRAY)));
            default -> text("This is a unique id type specified via the LuckPerms API").color(DARK_GRAY)
                    .hoverEvent(HoverEvent.showText(text("This is a unique id type specified via the LuckPerms API").color(DARK_GRAY)));
        };
    }
}

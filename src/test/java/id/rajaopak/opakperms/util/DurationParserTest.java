package id.rajaopak.opakperms.util;

import com.google.common.collect.ImmutableMap;
import id.rajaopak.opakperms.exception.ArgumentException;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class DurationParserTest {

    private static final Map<ChronoUnit, String> UNITS_PATTERNS = ImmutableMap.<ChronoUnit, String>builder()
            .put(ChronoUnit.YEARS, "y(?:ear)?s?")
            .put(ChronoUnit.MONTHS, "mo(?:nth)?s?")
            .put(ChronoUnit.WEEKS, "w(?:eek)?s?")
            .put(ChronoUnit.DAYS, "d(?:ay)?s?")
            .put(ChronoUnit.HOURS, "h(?:our|r)?s?")
            .put(ChronoUnit.MINUTES, "m(?:inute|in)?s?")
            .put(ChronoUnit.SECONDS, "s(?:econd|ec)?s?")
            .build();

    private static final ChronoUnit[] UNITS = UNITS_PATTERNS.keySet().toArray(new ChronoUnit[0]);

    private static final String PATTERN_STRING = UNITS_PATTERNS.values().stream()
            .map(pattern -> "(?:(\\d+)\\s*" + pattern + "[,\\s]*)?")
            .collect(Collectors.joining("", "^\\s*", "$"));

    public static void main(String[] args) {
        System.out.println(PATTERN_STRING);
        try {
            Duration parsed = DurationParser.parseDuration("1d10m10s");

            System.out.println(parsed.toSeconds());
            System.out.println(parsed.toMinutes());

            System.out.println(LegacyComponentSerializer.legacySection().serialize(DurationFormatter.LONG.format(parsed)));
        } catch (ArgumentException.InvalidDate e) {
            e.printStackTrace();
        }
    }

    @Test
    void parseDuration() throws ArgumentException.InvalidDate {
        Duration parsed = DurationParser.parseDuration("1d10m10s");

        assertEquals(LegacyComponentSerializer.legacySection().serialize(DurationFormatter.LONG.format(parsed)), "1 day 10 minutes 10 seconds");
    }
}
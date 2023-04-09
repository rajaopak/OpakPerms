package id.rajaopak.opakperms.util;

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DurationFormatterTest {

    @Test
    void format() {
        Duration duration = Duration.of(213, ChronoUnit.DAYS);

        assertEquals(LegacyComponentSerializer.legacySection().serialize(DurationFormatter.LONG.format(duration)), "6 months 4 weeks 2 days 9 hours 5 minutes 24 seconds");
    }
}
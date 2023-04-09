package id.rajaopak.opakperms.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

import static net.kyori.adventure.text.Component.text;

public class DurationFormatter {
    public static final DurationFormatter LONG = new DurationFormatter(false);
    public static final DurationFormatter CONCISE = new DurationFormatter(true);
    public static final DurationFormatter CONCISE_LOW_ACCURACY = new DurationFormatter(true, 3);

    private static final ChronoUnit[] UNITS = new ChronoUnit[]{
            ChronoUnit.YEARS,
            ChronoUnit.MONTHS,
            ChronoUnit.WEEKS,
            ChronoUnit.DAYS,
            ChronoUnit.HOURS,
            ChronoUnit.MINUTES,
            ChronoUnit.SECONDS
    };

    private final boolean concise;
    private final int accuracy;

    public DurationFormatter(boolean concise) {
        this(concise, Integer.MAX_VALUE);
    }

    public DurationFormatter(boolean concise, int accuracy) {
        this.concise = concise;
        this.accuracy = accuracy;
    }

    /**
     * Formats {@code duration} as a {@link Component}.
     *
     * @param duration the duration
     * @return the formatted component
     */
    public Component format(Duration duration) {
        long seconds = duration.getSeconds();
        TextComponent.Builder builder = text();
        int outputSize = 0;

        for (ChronoUnit unit : UNITS) {
            long n = seconds / unit.getDuration().getSeconds();
            if (n > 0) {
                seconds -= unit.getDuration().getSeconds() * n;
                if (outputSize != 0) {
                    builder.append(Component.space());
                }
                builder.append(Objects.requireNonNull(formatPart(n, unit)));
                outputSize++;
            }
            if (seconds <= 0 || outputSize >= this.accuracy) {
                break;
            }
        }

        if (outputSize == 0) {
            return formatPart(0, ChronoUnit.SECONDS);
        }
        return builder.build();
    }

    private Component formatPart(long amount, ChronoUnit unit) {
        switch (unit.name().toLowerCase()) {
            case "seconds" -> {
                return this.concise ? text(amount + "s") : amount == 1 ? text(amount + " second") : text(amount + " seconds");
            }
            case "minutes" -> {
                return this.concise ? text(amount + "m") : amount == 1 ? text(amount + " minute") : text(amount + " minutes");
            }
            case "hours" -> {
                return this.concise ? text(amount + "h") : amount == 1 ? text(amount + " hour") : text(amount + " hours");
            }
            case "days" -> {
                return this.concise ? text(amount + "d") : amount == 1 ? text(amount + " day") : text(amount + " days");
            }
            case "weeks" -> {
                return this.concise ? text(amount + "w") : amount == 1 ? text(amount + " week") : text(amount + " weeks");
            }
            case "months" -> {
                return this.concise ? text(amount + "mo") : amount == 1 ? text(amount + " month") : text(amount + " months");
            }
            case "years" -> {
                return this.concise ? text(amount + "y") : amount == 1 ? text(amount + " year") : text(amount + " years");
            }
            default -> {
                return null;
            }
        }
    }

}

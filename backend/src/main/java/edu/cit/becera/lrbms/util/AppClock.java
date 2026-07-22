package edu.cit.becera.lrbms.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Single source of truth for "today" across the app. The library operates in the Philippines, but
 * the server can run in any timezone (e.g. UTC on Render) - calling LocalDate.now() directly picks
 * up the JVM's default zone and can be a day behind/ahead of the actual local date for users here.
 */
public final class AppClock {

    public static final ZoneId ZONE = ZoneId.of("Asia/Manila");

    /** The library closes at 6 PM; a booking's pickup deadline is closing time on its pickup date. */
    public static final int CLOSING_HOUR = 18;

    private AppClock() {
    }

    public static LocalDate today() {
        return LocalDate.now(ZONE);
    }

    public static LocalDateTime now() {
        return LocalDateTime.now(ZONE);
    }

    public static LocalDateTime pickupDeadline(LocalDate pickupDate) {
        return pickupDate.atTime(CLOSING_HOUR, 0);
    }
}

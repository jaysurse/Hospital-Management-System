package com.hospital.util;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Date and Time conversion and formatting utility.
 * Bridges java.time (modern) and java.sql (JDBC) types seamlessly.
 */
public final class DateUtil {

    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter DISPLAY_DATE = DateTimeFormatter.ofPattern("dd MMM yyyy");
    public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    public static final DateTimeFormatter DISPLAY_TIME = DateTimeFormatter.ofPattern("hh:mm a");
    public static final DateTimeFormatter DISPLAY_DATETIME = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

    private DateUtil() {
    }

    public static LocalDate parseDate(String text) {
        if (text == null || text.trim().isEmpty()) return null;
        try {
            return LocalDate.parse(text.trim(), DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            try {
                return LocalDate.parse(text.trim(), DISPLAY_DATE);
            } catch (DateTimeParseException ex) {
                return null;
            }
        }
    }

    public static LocalTime parseTime(String text) {
        if (text == null || text.trim().isEmpty()) return null;
        try {
            return LocalTime.parse(text.trim(), TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            try {
                // Try format HH:mm
                return LocalTime.parse(text.trim(), DateTimeFormatter.ofPattern("HH:mm"));
            } catch (DateTimeParseException ex) {
                try {
                    return LocalTime.parse(text.trim(), DISPLAY_TIME);
                } catch (Exception ex2) {
                    return null;
                }
            }
        }
    }

    public static String formatDate(LocalDate date) {
        return date == null ? "-" : date.format(DISPLAY_DATE);
    }

    public static String formatIsoDate(LocalDate date) {
        return date == null ? "" : date.format(DATE_FORMATTER);
    }

    public static String formatTime(LocalTime time) {
        return time == null ? "-" : time.format(DISPLAY_TIME);
    }

    public static String formatIsoTime(LocalTime time) {
        return time == null ? "" : time.format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    public static String formatTimestamp(Timestamp ts) {
        if (ts == null) return "-";
        return ts.toLocalDateTime().format(DISPLAY_DATETIME);
    }

    public static Date toSqlDate(LocalDate localDate) {
        return localDate == null ? null : Date.valueOf(localDate);
    }

    public static LocalDate toLocalDate(Date sqlDate) {
        return sqlDate == null ? null : sqlDate.toLocalDate();
    }

    public static Time toSqlTime(LocalTime localTime) {
        return localTime == null ? null : Time.valueOf(localTime);
    }

    public static LocalTime toLocalTime(Time sqlTime) {
        return sqlTime == null ? null : sqlTime.toLocalTime();
    }

    public static Timestamp toSqlTimestamp(LocalDateTime ldt) {
        return ldt == null ? null : Timestamp.valueOf(ldt);
    }
}

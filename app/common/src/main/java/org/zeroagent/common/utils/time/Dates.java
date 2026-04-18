package org.zeroagent.common.utils.time;

import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalQuery;

import static java.time.format.DateTimeFormatter.ofPattern;

@UtilityClass
public class Dates {

    public static final String PATTERN_DATE                  = "yyyy-MM-dd";
    public static final String PATTERN_DATE_COMPACT          = "yyyyMMdd";
    public static final String PATTERN_DATE_TIME             = "yyyy-MM-dd HH:mm:ss";
    public static final String PATTERN_DATE_TIME_COMPACT     = "yyyyMMddHHmmss";
    public static final String PATTERN_DATE_TIME_WITH_MILLIS = "yyyy-MM-dd HH:mm:ss.SSS";

    public static final DateTimeFormatter FORMATTER_DATE                  = ofPattern(PATTERN_DATE);
    public static final DateTimeFormatter FORMATTER_DATE_COMPACT          = ofPattern(PATTERN_DATE_COMPACT);
    public static final DateTimeFormatter FORMATTER_DATE_TIME             = ofPattern(PATTERN_DATE_TIME);
    public static final DateTimeFormatter FORMATTER_DATE_TIME_COMPACT     = ofPattern(PATTERN_DATE_TIME_COMPACT);
    public static final DateTimeFormatter FORMATTER_DATE_TIME_WITH_MILLIS = ofPattern(PATTERN_DATE_TIME_WITH_MILLIS);

    public static ZonedDateTimeHelper of(LocalDate date) {
        return ZonedDateTimeHelper.of(date.atStartOfDay(ZoneId.systemDefault()));
    }

    public static ZonedDateTimeHelper of(int year, int month, int dayOfMonth) {
        return of(LocalDate.of(year, month, dayOfMonth));
    }

    public static ZonedDateTimeHelper of(LocalDateTime dateTime) {
        return ZonedDateTimeHelper.of(dateTime.atZone(ZoneId.systemDefault()));
    }

    public static ZonedDateTimeHelper of(ZonedDateTime dateTime) {
        return ZonedDateTimeHelper.of(dateTime);
    }

    public static ZonedDateTimeHelper of(Instant instant) {
        return ZonedDateTimeHelper.of(instant.atZone(ZoneId.systemDefault()));
    }

    public static ZonedDateTimeHelper ofMills(long timestamp) {
        return of(Instant.ofEpochMilli(timestamp));
    }

    public static ZonedDateTimeHelper of(TemporalAccessor temporal) {
        if (temporal instanceof LocalDate date) {
            return of(date);
        }
        if (temporal instanceof LocalDateTime dateTime) {
            return of(dateTime);
        }
        return ZonedDateTimeHelper.of(ZonedDateTime.from(temporal));
    }

    public static ZonedDateTimeHelper now() {
        return ZonedDateTimeHelper.of(ZonedDateTime.now(ZoneId.systemDefault()));
    }

    public static String currentDateTimeString() {
        return now().formatAsDateTime();
    }

    public static Parser of(String formattedValue) {
        return Parser.of(formattedValue);
    }

    /**
     * 计算两个时间的相差天数（算头不算尾）
     */
    public static long calcDays(Temporal startInclusive, Temporal endExclusive) {
        return startInclusive.until(endExclusive, ChronoUnit.DAYS);
    }

    @RequiredArgsConstructor(staticName = "of")
    public static class ZonedDateTimeHelper {

        private final ZonedDateTime dateTime;

        public LocalDate toDate() {
            return dateTime.withZoneSameInstant(ZoneId.systemDefault()).toLocalDate();
        }

        public LocalDateTime toDateTime() {
            return dateTime.withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
        }

        public ZonedDateTime toZonedDateTime() {
            return dateTime;
        }

        public Instant toInstant() {
            return dateTime.toInstant();
        }

        public long toMills() {
            return dateTime.toInstant().toEpochMilli();
        }

        public int toDateNumber() {
            return Integer.parseInt(formatAsDate(DateStyle.compact));
        }

        public long toDateTimeNumber() {
            return Integer.parseInt(formatAsDateTime(DateTimeStyle.compact));
        }

        public String formatAsDate() {
            return formatAsDate(DateStyle.normal);
        }

        public String formatAsDate(DateStyle style) {
            return switch (style) {
                case normal -> FORMATTER_DATE.format(dateTime);
                case compact -> FORMATTER_DATE_COMPACT.format(dateTime);
            };
        }

        public String formatAsDateTime() {
            return formatAsDateTime(DateTimeStyle.normal);
        }

        public String formatAsDateTime(DateTimeStyle style) {
            return switch (style) {
                case normal -> FORMATTER_DATE_TIME.format(dateTime);
                case compact -> FORMATTER_DATE_TIME_COMPACT.format(dateTime);
                case with_mills -> FORMATTER_DATE_TIME_WITH_MILLIS.format(dateTime);
            };
        }

//        public String format(String pattern) {
//            Asserts.notBlank(pattern, "pattern cannot be null");
//            return DateTimeFormatter.ofPattern(pattern).format(dateTime);
//        }

//        public boolean between(LocalDate startDate, LocalDate endDate) {
//            return between(startDate, endDate, Interval.LC_RO);
//        }
//
//        public boolean between(LocalDate startDate, LocalDate endDate, Interval interval) {
//            Asserts.notNull(startDate, "startDate cannot be null");
//            Asserts.notNull(endDate, "endDate cannot be null");
//            LocalDate date = toDate();
//            return switch (interval) {
//                case LO_RO -> date.isAfter(startDate) && date.isBefore(endDate);
//                case LO_RC -> date.isAfter(startDate) && !date.isAfter(endDate);
//                case LC_RO -> !date.isBefore(startDate) && date.isBefore(endDate);
//                default -> !date.isBefore(startDate) && !date.isAfter(endDate);
//            };
//        }
//
//        public boolean between(ZonedDateTime startDateTime, ZonedDateTime endDateTime) {
//            return between(startDateTime, endDateTime, Interval.LC_RO);
//        }
//
//        public boolean between(ZonedDateTime startDateTime, ZonedDateTime endDateTime, Interval interval) {
//            Asserts.notNull(startDateTime, "startDateTime cannot be null");
//            Asserts.notNull(endDateTime, "endDateTime cannot be null");
//            return switch (interval) {
//                case LO_RC -> dateTime.isAfter(startDateTime) && !dateTime.isAfter(endDateTime);
//                case LO_RO -> dateTime.isAfter(startDateTime) && dateTime.isBefore(endDateTime);
//                case LC_RO -> !dateTime.isBefore(startDateTime) && dateTime.isBefore(endDateTime);
//                default -> !dateTime.isBefore(startDateTime) && !dateTime.isAfter(endDateTime);
//            };
//        }
    }

    @RequiredArgsConstructor(staticName = "of")
    public static class Parser {

        private final String value;

        public LocalDate parseAsDate() {
            return parseAsDate(DateStyle.normal);
        }

        public LocalDate parseAsDate(DateStyle style) {
            return switch (style) {
                case normal -> FORMATTER_DATE.parse(value, LocalDate::from);
                case compact -> FORMATTER_DATE_COMPACT.parse(value, LocalDate::from);
            };
        }

        public LocalDateTime parseAsDateTime() {
            return parseAsDateTime(DateTimeStyle.normal);
        }

        public LocalDateTime parseAsDateTime(DateTimeStyle style) {
            return switch (style) {
                case normal -> FORMATTER_DATE_TIME.parse(value, LocalDateTime::from);
                case compact -> FORMATTER_DATE_TIME_COMPACT.parse(value, LocalDateTime::from);
                case with_mills -> FORMATTER_DATE_TIME_WITH_MILLIS.parse(value, LocalDateTime::from);
            };
        }

        public ZonedDateTime parseAsZonedDateTime() {
            return parseAsZonedDateTime(DateTimeStyle.normal);
        }

        public ZonedDateTime parseAsZonedDateTime(DateTimeStyle style) {
            LocalDateTime dateTime = parseAsDateTime(style);
            return dateTime.atZone(ZoneId.systemDefault());
        }

        public <T extends TemporalAccessor> T parse(String pattern, TemporalQuery<T> query) {
            return DateTimeFormatter.ofPattern(pattern).parse(value, query);
        }
    }
}


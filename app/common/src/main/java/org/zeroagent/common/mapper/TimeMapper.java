package org.zeroagent.common.mapper;

import org.checkerframework.checker.nullness.qual.PolyNull;
import org.jetbrains.annotations.Nullable;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import org.zeroagent.common.utils.time.Dates;

import java.time.*;

@Mapper
public interface TimeMapper {

    TimeMapper INSTANCE = Mappers.getMapper(TimeMapper.class);

    /**
     * LocalDateTime to OffsetDateTime
     *
     * @param localDateTime -
     * @return -
     */
    @PolyNull
    default OffsetDateTime toOffsetDateTime(@Nullable LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return localDateTime.atZone(ZoneId.systemDefault()).toOffsetDateTime();
    }

    /**
     * OffsetDateTime to LocalDateTime
     *
     * @param offsetDateTime -
     * @return -
     */
    @PolyNull
    default LocalDateTime toLocalDateTime(@Nullable OffsetDateTime offsetDateTime) {
        if (offsetDateTime == null) {
            return null;
        }
        return offsetDateTime.toLocalDateTime();
    }

    /**
     * LocalDateTime to ZonedDateTime
     *
     * @param localDateTime -
     * @return -
     */
    @PolyNull
    default ZonedDateTime toZonedDateTime(@Nullable LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return localDateTime.atZone(ZoneId.systemDefault());
    }

    /**
     * ZonedDateTime to LocalDateTime
     *
     * @param zonedDateTime -
     * @return -
     */
    @PolyNull
    default LocalDateTime toLocalDateTime(@Nullable ZonedDateTime zonedDateTime) {
        if (zonedDateTime == null) {
            return null;
        }
        return zonedDateTime.toLocalDateTime();
    }

    /**
     * timestamp to ZonedDateTime
     *
     * @param timestamp -
     * @return -
     */
    @PolyNull
    default ZonedDateTime toZonedDateTime(@Nullable Long timestamp) {
        if (timestamp == null) {
            return null;
        }
        return Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault());
    }

    /**
     * ZonedDateTime to timestamp
     *
     * @param zonedDateTime -
     * @return -
     */
    @PolyNull
    default Long toTimestamp(@Nullable ZonedDateTime zonedDateTime) {
        if (zonedDateTime == null) {
            return null;
        }
        return zonedDateTime.toInstant().toEpochMilli();
    }

    @PolyNull
    default String toDateTimeString(@Nullable ZonedDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return Dates.FORMATTER_DATE_TIME.format(dateTime);
    }

    @PolyNull
    default String toDateTimeString(@Nullable LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return Dates.FORMATTER_DATE_TIME.format(dateTime);
    }

    @PolyNull
    default Long toMills(@Nullable Duration duration) {
        if (duration == null) {
            return null;
        }
        return duration.toMillis();
    }
}

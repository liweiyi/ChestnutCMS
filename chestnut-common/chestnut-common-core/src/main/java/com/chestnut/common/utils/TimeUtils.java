package com.chestnut.common.utils;

import java.time.*;
import java.time.format.DateTimeFormatter;

/**
 * TimeUtils
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public class TimeUtils {

    public static final ZoneOffset ZONE_OFFSET = ZoneId.systemDefault().getRules().getOffset(Instant.now());

    public static final DateTimeFormatter YYYY_MM_DD_HH_MM_SS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static final DateTimeFormatter YYYY_MM_DD = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static OffsetDateTime offset() {
        return OffsetDateTime.now(ZONE_OFFSET);
    }

    /**
     * Instant to LocalDateTime
     */
    public static LocalDateTime toLocalDateTime(Instant instant) {
        return LocalDateTime.ofInstant(instant, ZONE_OFFSET);
    }

    public static String localDateTimeFormat(long epochMilli) {
        return YYYY_MM_DD_HH_MM_SS.format(toLocalDateTime(Instant.ofEpochMilli(epochMilli)));
    }

    public static String localDateFormat(long epochMilli) {
        return YYYY_MM_DD.format(toLocalDateTime(Instant.ofEpochMilli(epochMilli)));
    }

    public static String localFormat(Instant instant) {
        return YYYY_MM_DD_HH_MM_SS.format(toLocalDateTime(instant));
    }

    public static String format(LocalDateTime localDateTime) {
        return YYYY_MM_DD_HH_MM_SS.format(localDateTime);
    }

    public static String format(LocalDate localDate) {
        return YYYY_MM_DD.format(localDate);
    }

    public static Instant toInstant(LocalDateTime localDateTime) {
        return localDateTime.atZone(ZONE_OFFSET).toInstant();
    }
}

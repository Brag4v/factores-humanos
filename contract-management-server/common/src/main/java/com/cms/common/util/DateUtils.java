package com.cms.common.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public final class DateUtils {

    private DateUtils() {
        // Utility class - prevent instantiation
    }

    public static long daysBetween(LocalDate startDate, LocalDate endDate) {
        return ChronoUnit.DAYS.between(startDate, endDate);
    }

    public static long daysUntil(LocalDate targetDate) {
        return daysBetween(LocalDate.now(), targetDate);
    }

    public static boolean isExpiringSoon(LocalDate endDate, int daysThreshold) {
        long daysUntilExpiry = daysUntil(endDate);
        return daysUntilExpiry >= 0 && daysUntilExpiry <= daysThreshold;
    }

    public static boolean isExpired(LocalDate endDate) {
        return endDate.isBefore(LocalDate.now());
    }

    public static boolean isActive(LocalDate startDate, LocalDate endDate) {
        LocalDate today = LocalDate.now();
        return !today.isBefore(startDate) && !today.isAfter(endDate);
    }

    public static LocalDate addDays(LocalDate date, int days) {
        return date.plusDays(days);
    }

    public static LocalDate addMonths(LocalDate date, int months) {
        return date.plusMonths(months);
    }

    public static LocalDate addYears(LocalDate date, int years) {
        return date.plusYears(years);
    }

    public static LocalDateTime startOfDay(LocalDate date) {
        return date.atStartOfDay();
    }

    public static LocalDateTime endOfDay(LocalDate date) {
        return date.atTime(23, 59, 59);
    }

    public static boolean isWithinRange(LocalDate date, LocalDate startDate, LocalDate endDate) {
        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }
}

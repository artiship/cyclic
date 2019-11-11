package io.github.artiship.cyclic;

import java.time.temporal.ChronoUnit;

import static java.lang.Math.round;
import static java.time.temporal.ChronoUnit.*;

public enum JobCycle {

    MINUTE(1), HOUR(2), DAY(3), WEEK(4), MONTH(5), YEAR(6), NONE(7);

    private int code;

    JobCycle(int code) {
        this.code = code;
    }

    public static JobCycle of(int code) {
        for (JobCycle jobCycle : JobCycle.values()) {
            if (jobCycle.code == code) {
                return jobCycle;
            }
        }
        throw new IllegalArgumentException("unsupported job cycle " + code);
    }

    private static final long minute = 60_000;
    private static final long hour = 60 * minute;
    private static final long day = 24 * hour;
    private static final long week = 7 * day;
    private static final long month = 28 * day;
    private static final long year = 365 * day;

    public static JobCycle from(Long interval) {
        if (interval >= minute && interval < hour) return MINUTE;
        if (interval >= hour && interval < day) return HOUR;
        if (interval >= day && interval < week) return DAY;
        if (interval >= week && interval < month) return WEEK;
        if (interval >= month) return MONTH;

        return NONE;
    }

    public static Integer numberOfCycles(Long interval) {
        return round(interval / from(interval).cycleInterval());
    }

    public Long cycleInterval() {
        switch (this) {
            case MINUTE:
                return minute;
            case HOUR:
                return hour;
            case DAY:
                return day;
            case WEEK:
                return week;
            case MONTH:
                return month;
            case YEAR:
                return year;
        }

        return null;
    }

    public static ChronoUnit truncateUnit(Long interval) {
        switch (from(interval)) {
            case MINUTE:
                return MINUTES;
            case HOUR:
                return HOURS;
            case DAY:
                return DAYS;
            case WEEK:
                return WEEKS;
            case MONTH:
                return MONTHS;
            case YEAR:
                return YEARS;
        }

        return null;
    }

    public Integer historySize() {
        int size = 1;
        switch (this) {
            case DAY:
                size = 8;
                break;
            case HOUR:
                size = 25;
                break;
            case WEEK:
                size = 6;
                break;
            case MONTH:
                size = 1;
            case MINUTE:
                size = 60;
                break;
        }
        return size;
    }

    public int getCode() {
        return this.code;
    }
}
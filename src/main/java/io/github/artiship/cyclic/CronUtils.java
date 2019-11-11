package io.github.artiship.cyclic;

import org.quartz.impl.triggers.CronTriggerImpl;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import static java.time.Instant.ofEpochMilli;
import static java.time.ZoneId.systemDefault;
import static java.util.Date.from;
import static org.quartz.TriggerUtils.computeFireTimes;
import static org.quartz.TriggerUtils.computeFireTimesBetween;

/**
 * Compute schedule time by a given point.
 *
 * pre                of                  next
 * -2                 -1    sometime        0
 * |__________________|________|____________|
 * |____interval______|
 *
 */
public class CronUtils {

    public static LocalDateTime previousScheduleTimeOf(String cron, LocalDateTime sometime) {
        return scheduleTime(cron, sometime, -2);
    }

    public static LocalDateTime scheduleTimeOf(String cron, LocalDateTime sometime) {
        return scheduleTime(cron, sometime, -1);
    }

    public static LocalDateTime nextScheduleTimeOf(String cron, LocalDateTime sometime) {
        return scheduleTime(cron, sometime, 0);
    }

    private static LocalDateTime scheduleTime(String cron, LocalDateTime sometime, int offset) {
        CronTriggerImpl cronTrigger = getCronTrigger(cron);

        long interval = intervalOf(cronTrigger);

        Date from = from(sometime.atZone(systemDefault()).toInstant());
        Date to = new Date(from.getTime() + interval);

        List<Date> dates = computeFireTimesBetween(cronTrigger, null, from, to);
        Date next = dates.get(0);

        return ofEpochMilli(next.getTime() + interval * offset).atZone(systemDefault()).toLocalDateTime();
    }

    public static long intervalOf(String cron) {
        return intervalOf(getCronTrigger(cron));
    }

    public static long intervalOf(CronTriggerImpl cronTrigger) {
        List<Date> dates = computeFireTimes(cronTrigger, null, 2);
        Date next = dates.get(0);
        Date nextNext = dates.get(1);

        return nextNext.getTime() - next.getTime();
    }

    private static CronTriggerImpl getCronTrigger(String cron) {
        final CronTriggerImpl cronTrigger = new CronTriggerImpl();
        try {
            cronTrigger.setCronExpression(cron);
        } catch (ParseException e) {
            throw new RuntimeException("Cron expression is not valid");
        }
        return cronTrigger;
    }

    public static JobCycle jobCycle(String cron) {
        return JobCycle.from(intervalOf(cron));
    }
}

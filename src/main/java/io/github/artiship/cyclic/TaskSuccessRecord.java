package io.github.artiship.cyclic;

import lombok.Data;

import java.time.LocalDateTime;

import static io.github.artiship.cyclic.CronUtils.intervalOf;
import static java.time.temporal.ChronoUnit.SECONDS;
import static java.util.Objects.requireNonNull;

@Data
public class TaskSuccessRecord implements Comparable<TaskSuccessRecord> {

    private final LocalDateTime scheduleTime;
    private final String cronExpression;

    public static TaskSuccessRecord of(String cronExpression, LocalDateTime scheduleTime) {
        requireNonNull(cronExpression, "Cron is null");
        requireNonNull(scheduleTime, "Schedule time is null");

        return new TaskSuccessRecord(cronExpression, scheduleTime);
    }

    public TaskSuccessRecord(String cronExpression, LocalDateTime scheduleTime) {
        this.cronExpression = cronExpression;
        this.scheduleTime = scheduleTime;
    }

    public long interval() {
        return intervalOf(cronExpression);
    }

    @Override
    public int compareTo(TaskSuccessRecord lastRecord) {
        return scheduleTime.truncatedTo(SECONDS)
                           .compareTo(lastRecord.getScheduleTime()
                                                .truncatedTo(SECONDS));
    }

    public boolean cronEquals(String cronExpression) {
        return this.cronExpression.equals(cronExpression);
    }
}

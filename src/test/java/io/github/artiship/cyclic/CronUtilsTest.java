package io.github.artiship.cyclic;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.time.LocalDateTime;

import static io.github.artiship.cyclic.CronUtils.*;
import static java.time.temporal.ChronoUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class CronUtilsTest {


    @Test
    public void testScheduleTimeComputations() {
        String cron = "4 1 2 * * ?";
        LocalDateTime now = LocalDateTime.now();

        LocalDateTime previous = now.plusDays(-1).truncatedTo(SECONDS).withHour(2).withMinute(1).withSecond(4);
        LocalDateTime current = now.truncatedTo(SECONDS).withHour(2).withMinute(1).withSecond(4);
        LocalDateTime next = now.plusDays(1).truncatedTo(SECONDS).withHour(2).withMinute(1).withSecond(4);

        assertThat(previousScheduleTimeOf(cron, now)).isEqualTo(previous);
        assertThat(scheduleTimeOf(cron, now)).isEqualTo(current);
        assertThat(nextScheduleTimeOf(cron, now)).isEqualTo(next);

        log.info("previous: {}", previous);
        log.info("current: {}", current);
        log.info("next: {}", next);
    }

}
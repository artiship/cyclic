package io.github.artiship.cyclic;

import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.TreeSet;

import static io.github.artiship.cyclic.CronUtils.intervalOf;
import static io.github.artiship.cyclic.CronUtils.nextScheduleTimeOf;
import static io.github.artiship.cyclic.JobCycle.numberOfCycles;
import static io.github.artiship.cyclic.JobCycle.truncateUnit;
import static io.github.artiship.cyclic.TaskSuccessRecord.of;
import static java.time.format.DateTimeFormatter.ofPattern;
import static org.assertj.core.api.Assertions.assertThat;


@Slf4j
public class TaskDependencyTest {

    private TreeSet<TaskSuccessRecord> history = new TreeSet<>();

    @Test
    public void child_day_parent_day() {
        String parentCron = "4 1 2 * * ?"; //P 每天 02:01:04 执行
        String childCron  = "1 0 3 * * ?"; //C 每天 03:00:01 执行

        TaskSuccessRecord p1 = of(parentCron, parse("2019-11-09 02:01:04"));
        TaskSuccessRecord p2 = of(parentCron, parse("2019-11-10 02:01:04"));

        history.add(p1);
        history.add(p2);

        TaskSuccessRecord checkPoint = of(childCron,
                nextScheduleTimeOf(parentCron, checkPointBase("2019-11-10 03:00:01", parentCron)));

        TaskSuccessRecord checkPoint2 = of(childCron,
                nextScheduleTimeOf(parentCron, checkPointBase("2019-11-10 01:00:10", parentCron)));

        assertThat(history.ceiling(checkPoint)).isEqualTo(p2);
        assertThat(history.ceiling(checkPoint2)).isEqualTo(p2);
    }

    @Test
    public void child_day_parent_hour() {
        String parentCron = "4 1 */1 * * ?"; //P 每小时 01:04 执行
        String childCron  = "3 1 3 * * ?"; //C 每天 03:01:03 执行

        TaskSuccessRecord p1 = of(parentCron, parse("2019-11-09 22:01:04"));
        TaskSuccessRecord p2 = of(parentCron, parse("2019-11-09 23:01:04"));
        TaskSuccessRecord p3 = of(parentCron, parse("2019-11-10 01:01:04"));
        TaskSuccessRecord p4 = of(parentCron, parse("2019-11-10 02:01:04"));
        TaskSuccessRecord p5 = of(parentCron, parse("2019-11-10 03:01:04"));

        history.add(p1);
        history.add(p2);
        history.add(p3);
        history.add(p4);
        history.add(p5);

        TaskSuccessRecord checkPoint = of(childCron,
                nextScheduleTimeOf(parentCron, checkPointBase("2019-11-10 03:01:03", childCron)));

        assertThat(history.ceiling(checkPoint)).isEqualTo(p3);
    }

    @Test
    public void child_day_parent_hour_1_and_13() {
        String parentCron = "4 1 1,13 * * ?";
        String childCron  = "3 1 3 * * ?";

        TaskSuccessRecord p1 = of(parentCron, parse("2019-11-09 13:01:04"));
        TaskSuccessRecord p2 = of(parentCron, parse("2019-11-10 01:01:04"));

        history.add(p1);

        TaskSuccessRecord checkPoint = of(childCron,
                nextScheduleTimeOf(parentCron, checkPointBase("2019-11-10 03:01:03", childCron)));

        assertThat(history.higher(checkPoint)).isNull();

        history.add(p2);

        assertThat(history.higher(checkPoint)).isEqualTo(p2);
    }

    @Test
    public void child_hour_parent_hour_1_and_13() {
        String parentCron = "4 1 1,13 * * ?";
        String childCron  = "3 1 */1 * ?";

        TaskSuccessRecord p1 = of(parentCron, parse("2019-11-09 13:01:04"));
        TaskSuccessRecord p2 = of(parentCron, parse("2019-11-10 01:01:04"));

        history.add(p1);

        TaskSuccessRecord check_point_1 = of(childCron,
                nextScheduleTimeOf(parentCron, checkPointBase("2019-11-09 13:01:03", parentCron)));

        assertThat(history.higher(check_point_1)).isEqualTo(p1);

        TaskSuccessRecord check_point_2 = of(childCron,
                nextScheduleTimeOf(parentCron, checkPointBase("2019-11-09 14:01:03", parentCron)));

        assertThat(history.higher(check_point_2)).isEqualTo(p1);
    }

    private LocalDateTime checkPointBase(String scheduleTimeStr, String theGreaterCycleCron) {
        long interval = intervalOf(theGreaterCycleCron);

        ChronoUnit truncateUnit = truncateUnit(interval);
        Integer cycles = numberOfCycles(interval);

        return parse(scheduleTimeStr).truncatedTo(truncateUnit).minus(cycles - 1, truncateUnit);
    }

    @Test
    public void child_hour_parent_minute() {
        String parentCron  = "3 */5 * * * ?";
        String childCron = "4 5 */1 * * ?";

        TaskSuccessRecord p1 = of(parentCron, parse("2019-11-10 00:50:03"));
        TaskSuccessRecord p2 = of(parentCron, parse("2019-11-10 00:55:03"));
        TaskSuccessRecord p3 = of(parentCron, parse("2019-11-10 01:05:03"));

        history.add(p1);
        history.add(p2);

        TaskSuccessRecord checkPoint = of(childCron,
                nextScheduleTimeOf(parentCron, checkPointBase("2019-11-10 01:05:04", childCron)));

        assertThat(history.ceiling(checkPoint)).isNull();

        history.add(p3);

        assertThat(history.ceiling(checkPoint)).isEqualTo(p3);
    }

    @Test
    public void child_hour_parent_day() {
        String parentCron = "3 1 3 * * ?"; //P 每天 03:01:03 执行
        String childCron = "2 1 */1 * * ?"; //C 每小时 01:02 执行

        TaskSuccessRecord p1 = of(parentCron, parse("2019-11-09 03:01:03"));
        history.add(p1);

        TaskSuccessRecord check_point_1 = of(childCron,
                nextScheduleTimeOf(parentCron, checkPointBase("2019-11-10 03:01:02", parentCron)));

        assertThat(history.ceiling(check_point_1)).isNull();

        TaskSuccessRecord p2 = of(parentCron, parse("2019-11-10 03:01:03"));
        history.add(p2);

        TaskSuccessRecord check_point_2 = of(childCron,
                nextScheduleTimeOf(parentCron, checkPointBase("2019-11-10 04:01:02", parentCron)));

        assertThat(history.ceiling(check_point_2)).isEqualTo(p2);
    }

    @After
    public void after() {
        this.history.clear();
    }

    private LocalDateTime parse(String dateTimeStr) {
        return LocalDateTime.parse(dateTimeStr, ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    @Test
    @Ignore
    public void tree_set_correct_search_method() {
        TreeSet<Integer> set = new TreeSet<>();
        set.add(1);
        set.add(2);
        set.add(4);

        assertThat(set.ceiling(2)).isEqualTo(2); // ceiling includes equals
        assertThat(set.higher(2)).isEqualTo(4);
    }

}
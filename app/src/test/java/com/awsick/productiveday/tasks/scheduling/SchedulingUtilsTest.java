package com.awsick.productiveday.tasks.scheduling;

import static com.google.common.truth.Truth.assertThat;

import com.awsick.productiveday.tasks.models.Task;
import com.awsick.productiveday.tasks.models.Task.Type;
import com.awsick.productiveday.tasks.models.TaskRepeatability;
import com.awsick.productiveday.tasks.models.TaskRepeatability.EndType;
import com.awsick.productiveday.tasks.models.TaskRepeatability.PeriodType;
import com.awsick.productiveday.tasks.models.TaskRepeatability.Weekly;
import com.google.common.base.Optional;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import org.junit.Test;

public final class SchedulingUtilsTest {

  private static final Clock CLOCK = Clock.fixed(Instant.ofEpochMilli(0), ZoneId.systemDefault());

  // region Daily Reminders
  @Test
  public void testNextDeadline_daily_everyDay() {
    long deadline = getTimeInMillis(0, 1, 1);
    Task task =
        Task.builder()
            .setClock(CLOCK)
            .setTitle("title")
            .setType(Type.REMINDER)
            .setDeadlineMillis(deadline)
            .setRepeatability(
                TaskRepeatability.builder()
                    .setFrequency(1)
                    .setPeriodType(PeriodType.DAILY)
                    .setFirstReminder(deadline)
                    .setEndType(EndType.NEVER)
                    .build())
            .build();

    long nextDeadline = SchedulingUtils.getNextDeadline(task).get();

    assertThat(nextDeadline).isEqualTo(getTimeInMillis(0, 1, 2));
  }

  @Test
  public void testNextDeadline_daily_everyOtherDay() {
    long deadline = getTimeInMillis(0, 1, 1);
    int frequency = 2;
    Task task =
        Task.builder()
            .setClock(CLOCK)
            .setTitle("title")
            .setType(Type.REMINDER)
            .setDeadlineMillis(deadline)
            .setRepeatability(
                TaskRepeatability.builder()
                    .setFrequency(frequency)
                    .setPeriodType(PeriodType.DAILY)
                    .setFirstReminder(deadline)
                    .setEndType(EndType.NEVER)
                    .build())
            .build();

    long nextDeadline = SchedulingUtils.getNextDeadline(task).get();

    assertThat(nextDeadline).isEqualTo(getTimeInMillis(0, 1, 3));
  }

  @Test
  public void testNextDeadline_daily_wrapMonth() {
    long deadline = getTimeInMillis(0, 1, 31);
    Task task =
        Task.builder()
            .setClock(CLOCK)
            .setTitle("title")
            .setType(Type.REMINDER)
            .setDeadlineMillis(deadline)
            .setRepeatability(
                TaskRepeatability.builder()
                    .setFrequency(1)
                    .setPeriodType(PeriodType.DAILY)
                    .setFirstReminder(deadline)
                    .setEndType(EndType.NEVER)
                    .build())
            .build();

    long nextDeadline = SchedulingUtils.getNextDeadline(task).get();

    assertThat(nextDeadline).isEqualTo(getTimeInMillis(0, 2, 1));
  }

  @Test
  public void testNextDeadline_daily_wrapYear() {
    long deadline = getTimeInMillis(0, 12, 31);
    Task task =
        Task.builder()
            .setClock(CLOCK)
            .setTitle("title")
            .setType(Type.REMINDER)
            .setDeadlineMillis(deadline)
            .setRepeatability(
                TaskRepeatability.builder()
                    .setFrequency(1)
                    .setPeriodType(PeriodType.DAILY)
                    .setFirstReminder(deadline)
                    .setEndType(EndType.NEVER)
                    .build())
            .build();

    long nextDeadline = SchedulingUtils.getNextDeadline(task).get();

    assertThat(nextDeadline).isEqualTo(getTimeInMillis(1, 1, 1));
  }

  @Test
  public void testNextDeadline_daily_nthDeadline() {
    long deadline = getTimeInMillis(0, 1, 1);
    int occurrences = 2;
    Task task =
        Task.builder()
            .setClock(CLOCK)
            .setTitle("title")
            .setType(Type.REMINDER)
            .setDeadlineMillis(deadline)
            .setRepeatability(
                TaskRepeatability.builder()
                    .setFrequency(1)
                    .setPeriodType(PeriodType.DAILY)
                    .setFirstReminder(deadline)
                    .setEndType(EndType.AFTER)
                    .setEndAfterNTimes(Optional.of(occurrences))
                    .build())
            .build();

    // Test second occurrence is scheduled
    long nextDeadline = SchedulingUtils.getNextDeadline(task).get();
    assertThat(nextDeadline).isEqualTo(getTimeInMillis(0, 1, 2));

    // Test third occurrence isn't scheduled
    task = getTaskAtNextDeadline(task);
    assertThat(SchedulingUtils.getNextDeadline(task)).isAbsent();
  }

  @Test
  public void testNextDeadline_daily_afterDate() {
    long deadline = getTimeInMillis(0, 1, 1);
    Task task =
        Task.builder()
            .setClock(CLOCK)
            .setTitle("title")
            .setType(Type.REMINDER)
            .setDeadlineMillis(deadline)
            .setRepeatability(
                TaskRepeatability.builder()
                    .setFrequency(1)
                    .setPeriodType(PeriodType.DAILY)
                    .setFirstReminder(deadline)
                    .setEndType(EndType.ON)
                    .setEndOnTimeMillis(Optional.of(getTimeInMillis(0, 1, 2)))
                    .build())
            .build();

    // Test second occurrence is scheduled
    long nextDeadline = SchedulingUtils.getNextDeadline(task).get();
    assertThat(nextDeadline).isEqualTo(getTimeInMillis(0, 1, 2));

    // Test third occurrence isn't scheduled
    task = getTaskAtNextDeadline(task);
    assertThat(SchedulingUtils.getNextDeadline(task)).isAbsent();
  }
  // endregion

  // region Weekly Reminders
  @Test
  public void testNextDeadline_weekly_everyWeek() {
    long deadline = getTimeInMillis(0, 1, 3, DayOfWeek.MONDAY);

    Task task =
        Task.builder()
            .setClock(CLOCK)
            .setTitle("title")
            .setType(Type.REMINDER)
            .setDeadlineMillis(deadline)
            .setRepeatability(
                TaskRepeatability.builder()
                    .setFrequency(1)
                    .setPeriodType(PeriodType.WEEKLY)
                    .setWeekly(Optional.of(Weekly.builder().monday(true).build()))
                    .setFirstReminder(deadline)
                    .setEndType(EndType.NEVER)
                    .build())
            .build();

    long nextDeadline = SchedulingUtils.getNextDeadline(task).get();

    assertThat(nextDeadline).isEqualTo(getTimeInMillis(0, 1, 10, DayOfWeek.MONDAY));
  }

  @Test
  public void testNextDeadline_weekly_everyOtherWeek() {
    long deadline = getTimeInMillis(0, 1, 3, DayOfWeek.MONDAY);
    int frequency = 2;
    Task task =
        Task.builder()
            .setClock(CLOCK)
            .setTitle("title")
            .setType(Type.REMINDER)
            .setDeadlineMillis(deadline)
            .setRepeatability(
                TaskRepeatability.builder()
                    .setFrequency(frequency)
                    .setPeriodType(PeriodType.WEEKLY)
                    .setWeekly(Optional.of(Weekly.builder().monday(true).build()))
                    .setFirstReminder(deadline)
                    .setEndType(EndType.NEVER)
                    .build())
            .build();

    long nextDeadline = SchedulingUtils.getNextDeadline(task).get();

    assertThat(nextDeadline).isEqualTo(getTimeInMillis(0, 1, 3 + 14, DayOfWeek.MONDAY));
  }

  @Test
  public void testNextDeadline_weekly_withinWeek() {
    long deadline = getTimeInMillis(0, 1, 3, DayOfWeek.MONDAY);
    Task task =
        Task.builder()
            .setClock(CLOCK)
            .setTitle("title")
            .setType(Type.REMINDER)
            .setDeadlineMillis(deadline)
            .setRepeatability(
                TaskRepeatability.builder()
                    .setFrequency(1)
                    .setPeriodType(PeriodType.WEEKLY)
                    .setWeekly(Optional.of(Weekly.builder().monday(true).wednesday(true).build()))
                    .setFirstReminder(deadline)
                    .setEndType(EndType.NEVER)
                    .build())
            .build();

    long nextDeadline = SchedulingUtils.getNextDeadline(task).get();

    assertThat(nextDeadline).isEqualTo(getTimeInMillis(0, 1, 5, DayOfWeek.WEDNESDAY));
  }

  @Test
  public void testNextDeadline_weekly_wrapWeek() {
    long deadline = getTimeInMillis(0, 1, 5, DayOfWeek.WEDNESDAY);
    Task task =
        Task.builder()
            .setClock(CLOCK)
            .setTitle("title")
            .setType(Type.REMINDER)
            .setDeadlineMillis(deadline)
            .setRepeatability(
                TaskRepeatability.builder()
                    .setFrequency(1)
                    .setPeriodType(PeriodType.WEEKLY)
                    .setWeekly(Optional.of(Weekly.builder().monday(true).wednesday(true).build()))
                    .setFirstReminder(deadline)
                    .setEndType(EndType.NEVER)
                    .build())
            .build();

    long nextDeadline = SchedulingUtils.getNextDeadline(task).get();

    assertThat(nextDeadline).isEqualTo(getTimeInMillis(0, 1, 10, DayOfWeek.MONDAY));
  }

  @Test
  public void testNextDeadline_weekly_wrapMonth() {
    long deadline = getTimeInMillis(0, 1, 31, DayOfWeek.MONDAY);
    Task task =
        Task.builder()
            .setClock(CLOCK)
            .setTitle("title")
            .setType(Type.REMINDER)
            .setDeadlineMillis(deadline)
            .setRepeatability(
                TaskRepeatability.builder()
                    .setFrequency(1)
                    .setPeriodType(PeriodType.WEEKLY)
                    .setWeekly(Optional.of(Weekly.builder().monday(true).build()))
                    .setFirstReminder(deadline)
                    .setEndType(EndType.NEVER)
                    .build())
            .build();

    long nextDeadline = SchedulingUtils.getNextDeadline(task).get();

    assertThat(nextDeadline).isEqualTo(getTimeInMillis(0, 2, 7, DayOfWeek.MONDAY));
  }

  @Test
  public void testNextDeadline_weekly_wrapYear() {
    long deadline = getTimeInMillis(0, 12, 31, DayOfWeek.SUNDAY);
    Task task =
        Task.builder()
            .setClock(CLOCK)
            .setTitle("title")
            .setType(Type.REMINDER)
            .setDeadlineMillis(deadline)
            .setRepeatability(
                TaskRepeatability.builder()
                    .setFrequency(1)
                    .setPeriodType(PeriodType.WEEKLY)
                    .setWeekly(Optional.of(Weekly.builder().sunday(true).build()))
                    .setFirstReminder(deadline)
                    .setEndType(EndType.NEVER)
                    .build())
            .build();

    long nextDeadline = SchedulingUtils.getNextDeadline(task).get();

    assertThat(nextDeadline).isEqualTo(getTimeInMillis(1, 1, 7, DayOfWeek.SUNDAY));
  }

  @Test
  public void testNextDeadline_weekly_nthDeadline() {
    long deadline = getTimeInMillis(0, 1, 3, DayOfWeek.MONDAY);
    int occurrences = 2;
    Task task =
        Task.builder()
            .setClock(CLOCK)
            .setTitle("title")
            .setType(Type.REMINDER)
            .setDeadlineMillis(deadline)
            .setRepeatability(
                TaskRepeatability.builder()
                    .setFrequency(1)
                    .setPeriodType(PeriodType.WEEKLY)
                    .setWeekly(Optional.of(Weekly.builder().monday(true).build()))
                    .setFirstReminder(deadline)
                    .setEndType(EndType.AFTER)
                    .setEndAfterNTimes(Optional.of(occurrences))
                    .build())
            .build();

    // Test second occurrence is scheduled
    long nextDeadline = SchedulingUtils.getNextDeadline(task).get();
    assertThat(nextDeadline).isEqualTo(getTimeInMillis(0, 1, 10, DayOfWeek.MONDAY));

    // Test third occurrence isn't scheduled
    task = getTaskAtNextDeadline(task);
    assertThat(SchedulingUtils.getNextDeadline(task)).isAbsent();
  }

  @Test
  public void testNextDeadline_weekly_afterDate() {
    long deadline = getTimeInMillis(0, 1, 3, DayOfWeek.MONDAY);
    Task task =
        Task.builder()
            .setClock(CLOCK)
            .setTitle("title")
            .setType(Type.REMINDER)
            .setDeadlineMillis(deadline)
            .setRepeatability(
                TaskRepeatability.builder()
                    .setFrequency(1)
                    .setPeriodType(PeriodType.WEEKLY)
                    .setWeekly(Optional.of(Weekly.builder().monday(true).build()))
                    .setFirstReminder(deadline)
                    .setEndType(EndType.ON)
                    .setEndOnTimeMillis(Optional.of(getTimeInMillis(0, 1, 10, DayOfWeek.MONDAY)))
                    .build())
            .build();

    // Test second occurrence is scheduled
    long nextDeadline = SchedulingUtils.getNextDeadline(task).get();
    assertThat(nextDeadline).isEqualTo(getTimeInMillis(0, 1, 10, DayOfWeek.MONDAY));

    // Test third occurrence isn't scheduled
    task = getTaskAtNextDeadline(task);
    assertThat(SchedulingUtils.getNextDeadline(task)).isAbsent();
  }
  // endregion

  // region Monthly Reminders
  @Test
  public void testNextDeadline_monthly_everyMonth() {
    long deadline = getTimeInMillis(0, 1, 1);
    Task task =
        Task.builder()
            .setClock(CLOCK)
            .setTitle("title")
            .setType(Type.REMINDER)
            .setDeadlineMillis(deadline)
            .setRepeatability(
                TaskRepeatability.builder()
                    .setFrequency(1)
                    .setPeriodType(PeriodType.MONTHLY)
                    .setMonthly(Optional.of(1))
                    .setFirstReminder(deadline)
                    .setEndType(EndType.NEVER)
                    .build())
            .build();

    long nextDeadline = SchedulingUtils.getNextDeadline(task).get();

    assertThat(nextDeadline).isEqualTo(getTimeInMillis(0, 2, 1));
  }

  @Test
  public void testNextDeadline_monthly_everyOtherMonth() {
    long deadline = getTimeInMillis(0, 1, 1);
    int frequency = 2;
    Task task =
        Task.builder()
            .setClock(CLOCK)
            .setTitle("title")
            .setType(Type.REMINDER)
            .setDeadlineMillis(deadline)
            .setRepeatability(
                TaskRepeatability.builder()
                    .setFrequency(frequency)
                    .setPeriodType(PeriodType.MONTHLY)
                    .setMonthly(Optional.of(1))
                    .setFirstReminder(deadline)
                    .setEndType(EndType.NEVER)
                    .build())
            .build();

    long nextDeadline = SchedulingUtils.getNextDeadline(task).get();

    assertThat(nextDeadline).isEqualTo(getTimeInMillis(0, 3, 1));
  }

  @Test
  public void testNextDeadline_monthly_feb30th() {
    long deadline = getTimeInMillis(0, 1, 30);
    Task task =
        Task.builder()
            .setClock(CLOCK)
            .setTitle("title")
            .setType(Type.REMINDER)
            .setDeadlineMillis(deadline)
            .setRepeatability(
                TaskRepeatability.builder()
                    .setFrequency(1)
                    .setPeriodType(PeriodType.MONTHLY)
                    .setMonthly(Optional.of(1))
                    .setFirstReminder(deadline)
                    .setEndType(EndType.NEVER)
                    .build())
            .build();

    long nextDeadline = SchedulingUtils.getNextDeadline(task).get();

    assertThat(nextDeadline).isEqualTo(getTimeInMillis(0, 2, 29));
  }

  @Test
  public void testNextDeadline_monthly_everyOtherMonth_feb30th() {
    long deadline = getTimeInMillis(0, 1, 31);
    int frequency = 2;
    Task task =
        Task.builder()
            .setClock(CLOCK)
            .setTitle("title")
            .setType(Type.REMINDER)
            .setDeadlineMillis(deadline)
            .setRepeatability(
                TaskRepeatability.builder()
                    .setFrequency(frequency)
                    .setPeriodType(PeriodType.MONTHLY)
                    .setMonthly(Optional.of(1))
                    .setFirstReminder(deadline)
                    .setEndType(EndType.NEVER)
                    .build())
            .build();

    long nextDeadline = SchedulingUtils.getNextDeadline(task).get();

    assertThat(nextDeadline).isEqualTo(getTimeInMillis(0, 3, 31));
  }

  @Test
  public void testNextDeadline_monthly_twice() {
    long deadline = getTimeInMillis(0, 1, 1);
    Task task =
        Task.builder()
            .setClock(CLOCK)
            .setTitle("title")
            .setType(Type.REMINDER)
            .setDeadlineMillis(deadline)
            .setRepeatability(
                TaskRepeatability.builder()
                    .setFrequency(1)
                    .setPeriodType(PeriodType.MONTHLY)
                    .setMonthly(Optional.of(1))
                    .setFirstReminder(deadline)
                    .setEndType(EndType.NEVER)
                    .build())
            .build();
    task = getTaskAtNextDeadline(task);

    long nextDeadline = SchedulingUtils.getNextDeadline(task).get();

    assertThat(nextDeadline).isEqualTo(getTimeInMillis(0, 3, 1));
  }

  @Test
  public void testNextDeadline_monthly_wrapYear() {
    long deadline = getTimeInMillis(0, 12, 31);
    Task task =
        Task.builder()
            .setClock(CLOCK)
            .setTitle("title")
            .setType(Type.REMINDER)
            .setDeadlineMillis(deadline)
            .setRepeatability(
                TaskRepeatability.builder()
                    .setFrequency(1)
                    .setPeriodType(PeriodType.MONTHLY)
                    .setMonthly(Optional.of(1))
                    .setFirstReminder(deadline)
                    .setEndType(EndType.NEVER)
                    .build())
            .build();

    long nextDeadline = SchedulingUtils.getNextDeadline(task).get();

    assertThat(nextDeadline).isEqualTo(getTimeInMillis(1, 1, 31));
  }

  @Test
  public void testNextDeadline_monthly_nthDeadline() {
    long deadline = getTimeInMillis(0, 1, 1);
    int occurrences = 2;
    Task task =
        Task.builder()
            .setClock(CLOCK)
            .setTitle("title")
            .setType(Type.REMINDER)
            .setDeadlineMillis(deadline)
            .setRepeatability(
                TaskRepeatability.builder()
                    .setFrequency(1)
                    .setPeriodType(PeriodType.MONTHLY)
                    .setMonthly(Optional.of(1))
                    .setFirstReminder(deadline)
                    .setEndType(EndType.AFTER)
                    .setEndAfterNTimes(Optional.of(occurrences))
                    .build())
            .build();

    // Test second occurrence is scheduled
    long nextDeadline = SchedulingUtils.getNextDeadline(task).get();
    assertThat(nextDeadline).isEqualTo(getTimeInMillis(0, 2, 1));

    // Test third occurrence isn't scheduled
    task = getTaskAtNextDeadline(task);
    assertThat(SchedulingUtils.getNextDeadline(task)).isAbsent();
  }

  @Test
  public void testNextDeadline_monthly_afterDate() {
    long deadline = getTimeInMillis(0, 1, 1);
    Task task =
        Task.builder()
            .setClock(CLOCK)
            .setTitle("title")
            .setType(Type.REMINDER)
            .setDeadlineMillis(deadline)
            .setRepeatability(
                TaskRepeatability.builder()
                    .setFrequency(1)
                    .setPeriodType(PeriodType.MONTHLY)
                    .setMonthly(Optional.of(1))
                    .setFirstReminder(deadline)
                    .setEndType(EndType.ON)
                    .setEndOnTimeMillis(Optional.of(getTimeInMillis(0, 2, 1)))
                    .build())
            .build();

    // Test second occurrence is scheduled
    long nextDeadline = SchedulingUtils.getNextDeadline(task).get();
    assertThat(nextDeadline).isEqualTo(getTimeInMillis(0, 2, 1));

    // Test third occurrence isn't scheduled
    task = getTaskAtNextDeadline(task);
    assertThat(SchedulingUtils.getNextDeadline(task)).isAbsent();
  }
  // endregion

  // region Yearly Reminders
  @Test
  public void testNextDeadline_yearly_everyYear() {
    long deadline = getTimeInMillis(0, 1, 1);
    Task task =
        Task.builder()
            .setClock(CLOCK)
            .setTitle("title")
            .setType(Type.REMINDER)
            .setDeadlineMillis(deadline)
            .setRepeatability(
                TaskRepeatability.builder()
                    .setFrequency(1)
                    .setPeriodType(PeriodType.YEARLY)
                    .setFirstReminder(deadline)
                    .setEndType(EndType.NEVER)
                    .build())
            .build();

    long nextDeadline = SchedulingUtils.getNextDeadline(task).get();

    assertThat(nextDeadline).isEqualTo(getTimeInMillis(1, 1, 1));
  }

  @Test
  public void testNextDeadline_yearly_everyOtherYear() {
    long deadline = getTimeInMillis(0, 1, 1);
    int frequency = 2;
    Task task =
        Task.builder()
            .setClock(CLOCK)
            .setTitle("title")
            .setType(Type.REMINDER)
            .setDeadlineMillis(deadline)
            .setRepeatability(
                TaskRepeatability.builder()
                    .setFrequency(frequency)
                    .setPeriodType(PeriodType.YEARLY)
                    .setFirstReminder(deadline)
                    .setEndType(EndType.NEVER)
                    .build())
            .build();

    long nextDeadline = SchedulingUtils.getNextDeadline(task).get();

    assertThat(nextDeadline).isEqualTo(getTimeInMillis(2, 1, 1));
  }

  @Test
  public void testNextDeadline_yearly_leapYear() {
    long deadline = getTimeInMillis(0, 2, 29);
    Task task =
        Task.builder()
            .setClock(CLOCK)
            .setTitle("title")
            .setType(Type.REMINDER)
            .setDeadlineMillis(deadline)
            .setRepeatability(
                TaskRepeatability.builder()
                    .setFrequency(1)
                    .setPeriodType(PeriodType.YEARLY)
                    .setFirstReminder(deadline)
                    .setEndType(EndType.NEVER)
                    .build())
            .build();

    long nextDeadline = SchedulingUtils.getNextDeadline(task).get();

    assertThat(nextDeadline).isEqualTo(getTimeInMillis(1, 2, 28));
  }

  @Test
  public void testNextDeadline_yearly_every4Years_leapYear() {
    long deadline = getTimeInMillis(0, 2, 29);
    int frequency = 4;
    Task task =
        Task.builder()
            .setClock(CLOCK)
            .setTitle("title")
            .setType(Type.REMINDER)
            .setDeadlineMillis(deadline)
            .setRepeatability(
                TaskRepeatability.builder()
                    .setFrequency(frequency)
                    .setPeriodType(PeriodType.YEARLY)
                    .setFirstReminder(deadline)
                    .setEndType(EndType.NEVER)
                    .build())
            .build();

    long nextDeadline = SchedulingUtils.getNextDeadline(task).get();

    assertThat(nextDeadline).isEqualTo(getTimeInMillis(4, 2, 29));
  }

  @Test
  public void testNextDeadline_yearly_nthDeadline() {
    long deadline = getTimeInMillis(0, 1, 1);
    int occurrences = 2;
    Task task =
        Task.builder()
            .setClock(CLOCK)
            .setTitle("title")
            .setType(Type.REMINDER)
            .setDeadlineMillis(deadline)
            .setRepeatability(
                TaskRepeatability.builder()
                    .setFrequency(1)
                    .setPeriodType(PeriodType.YEARLY)
                    .setFirstReminder(deadline)
                    .setEndType(EndType.AFTER)
                    .setEndAfterNTimes(Optional.of(occurrences))
                    .build())
            .build();

    // Test second occurrence is scheduled
    long nextDeadline = SchedulingUtils.getNextDeadline(task).get();
    assertThat(nextDeadline).isEqualTo(getTimeInMillis(1, 1, 1));

    // Test third occurrence isn't scheduled
    task = getTaskAtNextDeadline(task);
    assertThat(SchedulingUtils.getNextDeadline(task)).isAbsent();
  }

  @Test
  public void testNextDeadline_yearly_afterDate() {
    long deadline = getTimeInMillis(0, 1, 1);
    Task task =
        Task.builder()
            .setClock(CLOCK)
            .setTitle("title")
            .setType(Type.REMINDER)
            .setDeadlineMillis(deadline)
            .setRepeatability(
                TaskRepeatability.builder()
                    .setFrequency(1)
                    .setPeriodType(PeriodType.YEARLY)
                    .setFirstReminder(deadline)
                    .setEndType(EndType.ON)
                    .setEndOnTimeMillis(Optional.of(getTimeInMillis(1, 1, 1)))
                    .build())
            .build();

    // Test second occurrence is scheduled
    long nextDeadline = SchedulingUtils.getNextDeadline(task).get();
    assertThat(nextDeadline).isEqualTo(getTimeInMillis(1, 1, 1));

    // Test third occurrence isn't scheduled
    task = getTaskAtNextDeadline(task);
    assertThat(SchedulingUtils.getNextDeadline(task)).isAbsent();
  }
  // endregion

  private static long getTimeInMillis(int yearSince1970, int month, int day) {
    return ZonedDateTime.of(yearSince1970, month, day, 0, 0, 0, 0, ZoneId.systemDefault())
        .toInstant()
        .toEpochMilli();
  }

  private static long getTimeInMillis(int yearSince1970, int month, int day, DayOfWeek dayOfWeek) {
    ZonedDateTime zdt =
        ZonedDateTime.of(yearSince1970, month, day, 0, 0, 0, 0, ZoneId.systemDefault());
    assertThat(zdt.getDayOfWeek()).isEqualTo(dayOfWeek);
    return zdt.toInstant().toEpochMilli();
  }

  private static Task getTaskAtNextDeadline(Task task) {
    return Task.builder()
        .setClock(CLOCK)
        .setTitle(task.title())
        .setType(task.type())
        // Set the updated deadline
        .setDeadlineMillis(SchedulingUtils.getNextDeadline(task).get())
        .setRepeatability(
            TaskRepeatability.builder()
                .setFrequency(task.repeatability().frequency())
                .setPeriodType(task.repeatability().periodType())
                .setWeekly(task.repeatability().weekly())
                .setMonthly(task.repeatability().monthly())
                .setFirstReminder(task.repeatability().firstReminder())
                .setEndType(task.repeatability().endType())
                .setEndAfterNTimes(task.repeatability().endAfterNTimes())
                .setEndOnTimeMillis(task.repeatability().endOnTimeMillis())
                .build())
        .build();
  }
}

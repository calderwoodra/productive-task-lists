package com.awsick.productiveday.tasks.scheduling;

import com.awsick.productiveday.tasks.models.Task;
import com.awsick.productiveday.tasks.models.TaskRepeatability;
import com.awsick.productiveday.tasks.models.TaskRepeatability.EndType;
import com.awsick.productiveday.tasks.models.TaskRepeatability.Weekly;
import com.google.common.base.Optional;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public final class SchedulingUtils {

  /** Returns the next deadline for the task if it should be rescheduled. */
  public static Optional<Long> getNextDeadline(Task task) {
    if (task.deadlineMillis() == -1 || task.repeatability() == null) {
      return Optional.absent();
    }

    long nextDeadline = getNextDeadlineInternal(task.repeatability(), task.deadlineMillis());

    TaskRepeatability repeatability = task.repeatability();
    if (repeatability.endType() == EndType.ON
        && nextDeadline > repeatability.endOnTimeMillis().get()) {
      // The user requested us to stop reminding them after this time
      return Optional.absent();
    }

    if (repeatability.endType() == EndType.AFTER) {
      long nthDeadline = repeatability.firstReminder();
      for (int i = 2; i <= repeatability.endAfterNTimes().get(); i++) {
        nthDeadline = getNextDeadlineInternal(repeatability, nthDeadline);
        if (nthDeadline > nextDeadline) {
          break;
        }
      }

      if (nextDeadline > nthDeadline) {
        // The user requested us to stop scheduling the reminder after N occurrences
        return Optional.absent();
      }
    }

    // At this point, the reminder needs to be scheduled again
    return Optional.of(nextDeadline);
  }

  private static long getNextDeadlineInternal(TaskRepeatability repeatability, long lastDeadline) {
    switch (repeatability.periodType()) {
      case DAILY:
        return lastDeadline + TimeUnit.DAYS.toMillis(repeatability.frequency());
      case WEEKLY:
        return computeWeekly(lastDeadline, repeatability);
      case MONTHLY:
        ZonedDateTime first =
            Instant.ofEpochMilli(repeatability.firstReminder()).atZone(ZoneId.systemDefault());
        ZonedDateTime latest = Instant.ofEpochMilli(lastDeadline).atZone(ZoneId.systemDefault());

        int months =
            Math.toIntExact(
                ChronoUnit.MONTHS.between(
                    YearMonth.of(first.getYear(), first.getMonth()),
                    YearMonth.of(latest.getYear(), latest.getMonth())));

        return first.plusMonths(months + repeatability.frequency()).toInstant().toEpochMilli();
      case YEARLY:
        ZonedDateTime firstYear =
            Instant.ofEpochMilli(repeatability.firstReminder()).atZone(ZoneId.systemDefault());
        ZonedDateTime lastYear = Instant.ofEpochMilli(lastDeadline).atZone(ZoneId.systemDefault());

        int years =
            Math.toIntExact(
                ChronoUnit.YEARS.between(
                    YearMonth.of(firstYear.getYear(), firstYear.getMonth()),
                    YearMonth.of(lastYear.getYear(), lastYear.getMonth())));

        return firstYear.plusYears(years + repeatability.frequency()).toInstant().toEpochMilli();
      default:
        throw new IllegalArgumentException("Unhandled period type: " + repeatability.periodType());
    }
  }

  private static long computeWeekly(long deadline, TaskRepeatability repeatability) {
    Weekly weekly = repeatability.weekly().get();
    List<DayOfWeek> daysOfWeek = new ArrayList<>();
    if (weekly.sunday()) {
      daysOfWeek.add(DayOfWeek.SUNDAY);
    }
    if (weekly.monday()) {
      daysOfWeek.add(DayOfWeek.MONDAY);
    }
    if (weekly.tuesday()) {
      daysOfWeek.add(DayOfWeek.TUESDAY);
    }
    if (weekly.wednesday()) {
      daysOfWeek.add(DayOfWeek.WEDNESDAY);
    }
    if (weekly.thursday()) {
      daysOfWeek.add(DayOfWeek.THURSDAY);
    }
    if (weekly.friday()) {
      daysOfWeek.add(DayOfWeek.FRIDAY);
    }
    if (weekly.saturday()) {
      daysOfWeek.add(DayOfWeek.SATURDAY);
    }

    ZonedDateTime zdt = Instant.ofEpochMilli(deadline).atZone(ZoneId.systemDefault());
    int position = daysOfWeek.indexOf(zdt.getDayOfWeek());

    // If we're at the end of the week, skip forward `frequency` weeks then roll back the date until
    // calendar dow matches first dow in repeatability (this covers reminders that only repeat on
    // one dow).
    //
    // Example, today is Tuesday, next reminder is Monday
    // Example, today is Tuesday, next reminder is Tuesday (next week)
    // Example, today is Tuesday, next reminder is Monday (skipping 2 weeks)
    // Example, today is Tuesday, next reminder is Tuesday (skipping 2 weeks)
    if (position == daysOfWeek.size() - 1) {
      zdt = zdt.plusWeeks(repeatability.frequency());
      while (zdt.getDayOfWeek() != daysOfWeek.get(0)) {
        zdt = zdt.minusDays(1);
      }
      return zdt.toInstant().toEpochMilli();
    }

    // There's another reminder this week, so skip forward a few days until days of week match
    //
    // Example, today is Tuesday, next reminder is Wednesday
    position++;
    while (zdt.getDayOfWeek() != daysOfWeek.get(position)) {
      zdt = zdt.plusDays(1);
    }
    return zdt.toInstant().toEpochMilli();
  }

  private SchedulingUtils() {}
}

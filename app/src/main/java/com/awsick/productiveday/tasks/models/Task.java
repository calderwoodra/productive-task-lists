package com.awsick.productiveday.tasks.models;

import androidx.annotation.Nullable;
import com.awsick.productiveday.common.utils.Assert;
import com.google.auto.value.AutoValue;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

@AutoValue
public abstract class Task {

  public enum Type {
    DEADLINE,
    REMINDER,
    UNSCHEDULED,
  }

  public abstract int uid();

  public abstract String title();

  public abstract String notes();

  public abstract Type type();

  /**
   * The next time the user will been notified about this task. For tasks with repeatability, this
   * value will change after each notification until the task should no longer repeat.
   */
  public abstract long deadlineMillis();

  /** @see Task.Builder#getDeadlineDistance(Clock, long) */
  public abstract String deadlineDistance();

  @Nullable
  public abstract TaskRepeatability repeatability();

  public abstract int directoryId();

  public abstract Clock clock();

  /**
   * Returns false if the user should be shown a notification when at or past {@link
   * #deadlineMillis()}, otherwise true.
   */
  public abstract boolean notified();

  public static Task.Builder builder() {
    return new AutoValue_Task.Builder()
        .setUid(0)
        .setNotes("")
        .setRepeatability(null)
        .setDirectoryId(-1)
        .setNotified(false)
        .setType(Type.UNSCHEDULED)
        .setDeadlineMillis(-1);
  }

  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder setUid(int uid);

    public abstract Builder setTitle(String title);

    public abstract Builder setNotes(String notes);

    public abstract Builder setType(Type type);

    public abstract Builder setDeadlineMillis(long deadlineMillis);

    abstract long deadlineMillis();

    abstract Builder setDeadlineDistance(String distance);

    public abstract Builder setRepeatability(@Nullable TaskRepeatability repeatability);

    public abstract Builder setDirectoryId(int directoryId);

    public abstract Builder setNotified(boolean notified);

    public abstract Builder setClock(Clock clock);

    abstract Clock clock();

    public abstract Task create();

    public Task build() {
      setDeadlineDistance(getDeadlineDistance(clock(), deadlineMillis()));
      Task task = create();
      // FREE tasks should not have a deadline set. DEADLINE/REMINDER tasks should have a deadline.
      Assert.checkArgument((task.deadlineMillis() == -1) == (task.type() == Type.UNSCHEDULED));
      return task;
    }

    private static String getDeadlineDistance(Clock clock, long deadline) {
      if (deadline == -1) {
        return "";
      }

      int daysRemaining = getDaysFromToday(clock, deadline);
      if (daysRemaining < 0) {
        return "past";
      } else if (daysRemaining == 0) {
        return "today";
      } else if (daysRemaining == 1) {
        return "tmw";
      } else {
        return daysRemaining + "d";
      }
    }
  }

  /**
   * Returns number of days from the deadline.
   *
   * <p>(-inf, -1] is a missed deadline 0 is a deadline of today [1, +inf) is a future deadline
   */
  public int getDaysFromToday() {
    return getDaysFromToday(clock(), deadlineMillis());
  }

  private static int getDaysFromToday(Clock clock, long deadline) {
    Assert.checkArgument(deadline != -1, "Task does not have a deadline");
    return Math.toIntExact(
        ChronoUnit.DAYS.between(
            clock.instant(), Instant.ofEpochMilli(deadline).atZone(ZoneId.systemDefault())));
  }
}

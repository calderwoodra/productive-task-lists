package com.awsick.productiveday.tasks.models;

import androidx.annotation.NonNull;
import com.awsick.productiveday.common.utils.Assert;
import com.google.auto.value.AutoValue;
import com.google.common.base.Optional;
import java.util.Calendar;

@AutoValue
public abstract class TaskRepeatability {

  public enum PeriodType {
    DAILY,
    WEEKLY,
    MONTHLY,
    YEARLY,
  }

  public enum EndType {
    /** Never stop reminding. */
    NEVER,
    /** Stop reminding after a date. {@link #endOnTimeMillis()}. */
    ON,
    /** Stop reminding after N occurrences. {@link #endAfterNTimes()}. */
    AFTER,
  }

  /**
   * The first time the user was/will be reminded about this task.
   *
   * <p>This value should never be updated. To know the next time the user will be notified, see
   * {@link Task#deadlineMillis()}.
   */
  public abstract long firstReminder();

  public abstract int frequency();

  /** How often the reminder should repeat (monthly, daily, etc.). */
  public abstract PeriodType periodType();

  public abstract Optional<Weekly> weekly();

  public abstract Optional<Integer> monthly();

  public abstract EndType endType();

  public abstract Optional<Long> endOnTimeMillis();

  public abstract Optional<Integer> endAfterNTimes();

  /**
   * Required params:
   *
   * <ul>
   *   <li>{@link #firstReminder()}
   *   <li>{@link #periodType()}
   *   <li>{@link #endType()}
   * </ul>
   */
  public static TaskRepeatability.Builder builder() {
    return new AutoValue_TaskRepeatability.Builder()
        .setWeekly(Optional.absent())
        .setMonthly(Optional.absent())
        .setEndOnTimeMillis(Optional.absent())
        .setEndAfterNTimes(Optional.absent());
  }

  @NonNull
  @Override
  public String toString() {
    StringBuilder toString = new StringBuilder("Repeats");
    switch (periodType()) {
      case DAILY:
        if (frequency() == 1) {
          toString.append(" daily");
        } else {
          toString.append(" every " + frequency() + " days");
        }
        break;
      case WEEKLY:
        if (frequency() == 1) {
          toString.append(" weekly on " + weekly().get().toString());
        } else {
          toString.append(" every " + frequency() + " weeks on " + weekly().get().toString());
        }
        break;
      case MONTHLY:
        if (frequency() == 1) {
          toString.append(" monthly");
        } else {
          toString.append(" every " + frequency() + " months");
        }
        break;
      case YEARLY:
        if (frequency() == 1) {
          toString.append(" yearly");
        } else {
          toString.append(" every " + frequency() + " years");
        }
        break;
    }

    switch (endType()) {
      case NEVER:
        break;
      case ON:
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(endOnTimeMillis().get());
        toString
            .append("; until ")
            .append(calendar.get(Calendar.MONTH) + 1)
            .append("/")
            .append(calendar.get(Calendar.DAY_OF_MONTH));
        break;
      case AFTER:
        toString
            .append("; ")
            .append(endAfterNTimes().get())
            .append(endAfterNTimes().get() > 1 ? " times" : " time");
        break;
    }
    return toString.toString();
  }

  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder setFirstReminder(long firstReminder);

    public abstract Builder setFrequency(int frequency);

    public abstract Builder setPeriodType(PeriodType periodType);

    public abstract Builder setWeekly(Optional<Weekly> weekly);

    public abstract Builder setMonthly(Optional<Integer> monthly);

    public abstract Builder setEndType(EndType endType);

    public abstract Builder setEndOnTimeMillis(Optional<Long> endOnTimeMillis);

    public abstract Builder setEndAfterNTimes(Optional<Integer> endAfterNTimes);

    public abstract TaskRepeatability build();
  }

  @AutoValue
  public abstract static class Weekly {

    public abstract boolean monday();

    public abstract boolean tuesday();

    public abstract boolean wednesday();

    public abstract boolean thursday();

    public abstract boolean friday();

    public abstract boolean saturday();

    public abstract boolean sunday();

    public static TaskRepeatability.Weekly.Builder builder() {
      return new AutoValue_TaskRepeatability_Weekly.Builder()
          .monday(false)
          .tuesday(false)
          .wednesday(false)
          .thursday(false)
          .friday(false)
          .saturday(false)
          .sunday(false);
    }

    @NonNull
    @Override
    public String toString() {
      StringBuilder toString = new StringBuilder();
      toString.append(monday() ? "Mon, " : "");
      toString.append(tuesday() ? "Tue, " : "");
      toString.append(wednesday() ? "Wed, " : "");
      toString.append(thursday() ? "Thu, " : "");
      toString.append(friday() ? "Fri, " : "");
      toString.append(saturday() ? "Sat, " : "");
      toString.append(sunday() ? "Sun, " : "");
      return toString.substring(0, toString.length() - 2);
    }

    @AutoValue.Builder
    public abstract static class Builder {

      public abstract TaskRepeatability.Weekly.Builder monday(boolean monday);

      public abstract TaskRepeatability.Weekly.Builder tuesday(boolean tuesday);

      public abstract TaskRepeatability.Weekly.Builder wednesday(boolean wednesday);

      public abstract TaskRepeatability.Weekly.Builder thursday(boolean thursday);

      public abstract TaskRepeatability.Weekly.Builder friday(boolean friday);

      public abstract TaskRepeatability.Weekly.Builder saturday(boolean saturday);

      public abstract TaskRepeatability.Weekly.Builder sunday(boolean sunday);

      abstract TaskRepeatability.Weekly create();

      public TaskRepeatability.Weekly build() {
        TaskRepeatability.Weekly weekly = create();
        Assert.checkState(
            weekly.monday()
                || weekly.tuesday()
                || weekly.wednesday()
                || weekly.thursday()
                || weekly.friday()
                || weekly.saturday()
                || weekly.sunday());
        return weekly;
      }
    }
  }
}

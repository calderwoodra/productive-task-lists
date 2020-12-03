package com.awsick.productiveday.tasks.models;

import androidx.annotation.Nullable;
import com.awsick.productiveday.common.utils.Assert;
import com.awsick.productiveday.common.utils.DateUtils;
import com.google.auto.value.AutoValue;
import java.util.concurrent.TimeUnit;

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

  public abstract long deadlineMillis();

  public abstract String deadlineDistance();

  @Nullable
  public abstract TaskRepeatability repeatability();

  public abstract int directoryId();

  public static Task.Builder builder() {
    return new AutoValue_Task.Builder()
        .setUid(0)
        .setNotes("")
        .setRepeatability(null)
        .setDirectoryId(-1)
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

    public abstract Task create();

    public Task build() {
      setDeadlineDistance(getDeadlineDistance(deadlineMillis()));
      Task task = create();
      // FREE tasks should not have a deadline set. DEADLINE/REMINDER tasks should have a deadline.
      Assert.checkArgument((task.deadlineMillis() == -1) == (task.type() == Type.UNSCHEDULED));
      return task;
    }

    private static String getDeadlineDistance(long deadline) {
      if (deadline == -1) {
        return "";
      }

      long now = DateUtils.getCurrentTimeMs();
      int daysRemaining = -1;
      while (deadline > now) {
        deadline -= TimeUnit.DAYS.toMillis(1);
        daysRemaining++;
      }

      switch (daysRemaining) {
        case -1:
          return "past";
        case 0:
          return "today";
        case 1:
          return "tmw";
        default:
          return daysRemaining + "d";
      }
    }
  }
}

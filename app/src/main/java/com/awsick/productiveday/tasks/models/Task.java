package com.awsick.productiveday.tasks.models;

import androidx.annotation.Nullable;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class Task {

  public abstract int uid();

  public abstract String title();

  public abstract String notes();

  public abstract long deadlineMillis();

  @Nullable
  public abstract TaskRepeatability repeatability();

  public abstract int directoryId();

  public static Task.Builder builder() {
    return new AutoValue_Task.Builder()
        .setUid(0)
        .setNotes("")
        .setRepeatability(null)
        .setDirectoryId(-1)
        .setDeadlineMillis(-1);
  }

  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder setUid(int uid);

    public abstract Builder setTitle(String title);

    public abstract Builder setNotes(String notes);

    public abstract Builder setDeadlineMillis(long deadlineMillis);

    public abstract Builder setRepeatability(@Nullable TaskRepeatability repeatability);

    public abstract Builder setDirectoryId(int directoryId);

    public abstract Task build();
  }
}

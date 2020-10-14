package com.awsick.productiveday.tasks.models;

import androidx.annotation.Nullable;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class Task {

  public abstract String title();

  public abstract String notes();

  public abstract long startTimeMillis();

  @Nullable
  public abstract TaskRepeatability repeatability();

  public abstract String directoryId();

  public static Task.Builder builder() {
    return new AutoValue_Task.Builder();
  }

  @AutoValue.Builder
  public static abstract class Builder {

    public abstract Builder setTitle(String title);

    public abstract Builder setNotes(String notes);

    public abstract Builder setStartTimeMillis(long startTimeMillis);

    public abstract Builder setRepeatability(@Nullable TaskRepeatability repeatability);

    public abstract Builder setDirectoryId(String directoryId);

    public abstract Task build();
  }
}

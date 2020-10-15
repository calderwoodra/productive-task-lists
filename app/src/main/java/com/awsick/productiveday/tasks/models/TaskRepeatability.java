package com.awsick.productiveday.tasks.models;

import androidx.annotation.NonNull;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class TaskRepeatability {

  public abstract boolean monday();

  public abstract boolean tuesday();

  public abstract boolean wednesday();

  public abstract boolean thursday();

  public abstract boolean friday();

  public abstract boolean saturday();

  public abstract boolean sunday();

  public static TaskRepeatability.Builder builder() {
    return new AutoValue_TaskRepeatability.Builder()
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
    return super.toString();
  }

  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder monday(boolean monday);

    public abstract Builder tuesday(boolean tuesday);

    public abstract Builder wednesday(boolean wednesday);

    public abstract Builder thursday(boolean thursday);

    public abstract Builder friday(boolean friday);

    public abstract Builder saturday(boolean saturday);

    public abstract Builder sunday(boolean sunday);

    public abstract TaskRepeatability build();
  }
}

package com.awsick.productiveday.directories.models;

import com.awsick.productiveday.tasks.models.Task;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;

@AutoValue
public abstract class Directory {

  public abstract DirectoryReference reference();

  public abstract ImmutableList<Task> tasks();

  public abstract ImmutableList<DirectoryReference> directories();

  public static Directory.Builder builder() {
    return new AutoValue_Directory.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder setReference(DirectoryReference reference);

    public abstract Builder setTasks(ImmutableList<Task> tasks);

    public abstract Builder setDirectories(ImmutableList<DirectoryReference> directories);

    public abstract Directory build();
  }
}

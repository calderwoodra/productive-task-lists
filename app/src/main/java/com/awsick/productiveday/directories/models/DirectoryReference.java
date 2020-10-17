package com.awsick.productiveday.directories.models;

import com.google.auto.value.AutoValue;
import com.google.common.base.Optional;

@AutoValue
public abstract class DirectoryReference {

  public abstract int uid();

  public abstract String name();

  public abstract Optional<DirectoryReference> parent();

  public static DirectoryReference.Builder builder() {
    return new AutoValue_DirectoryReference.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder setUid(int uid);

    public abstract Builder setName(String name);

    public abstract Builder setParent(Optional<DirectoryReference> parent);

    public abstract DirectoryReference build();
  }
}

package com.awsick.productiveday.directories.repo.room;

import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import com.awsick.productiveday.directories.models.DirectoryReference;
import com.google.common.base.Optional;

@Entity
public class DirectoryEntity {

  @PrimaryKey(autoGenerate = true)
  public int uid;

  @ColumnInfo(name = "name")
  public String name;

  @Nullable
  @ColumnInfo(name = "parent_uid")
  public Integer parentUid;

  public static DirectoryEntity from(DirectoryReference reference) {
    DirectoryEntity entity = new DirectoryEntity();
    entity.uid = reference.uid();
    entity.name = reference.name();
    entity.parentUid = reference.parent().isPresent() ? reference.parent().get() : null;
    return entity;
  }

  public DirectoryReference toDirectoryReference() {
    return DirectoryReference.builder()
        .setUid(uid)
        .setName(name)
        .setParent(Optional.fromNullable(parentUid))
        .build();
  }
}

package com.awsick.productiveday.directories.repo.room;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.google.common.util.concurrent.ListenableFuture;

@Dao
public interface DirectoryDao {

  // CREATE
  @Insert
  ListenableFuture<Long> insert(DirectoryEntity directory);

  // READ

  /** Return directory with {@code uid}. */
  @Query("SELECT * FROM directoryentity WHERE uid IS :uid")
  ListenableFuture<DirectoryEntity> getDirectory(int uid);

  /** Return all directories that have parent {@code uid}. */
  @Query("SELECT * FROM directoryentity WHERE parent_uid IS :uid")
  ListenableFuture<DirectoryEntity> getSiblingDirectories(int uid);

  // UPDATE
  @Update
  ListenableFuture<Void> update(DirectoryEntity... directories);

  // DELETE
  @Delete
  ListenableFuture<Void> delete(DirectoryEntity... directories);
}

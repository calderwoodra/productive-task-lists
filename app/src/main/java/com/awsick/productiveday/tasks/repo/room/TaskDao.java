package com.awsick.productiveday.tasks.repo.room;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.List;

@Dao
public interface TaskDao {

  // CREATE
  @Insert
  ListenableFuture<Long> insert(TaskEntity task);

  // READ
  @Query("SELECT * FROM taskentity")
  ListenableFuture<List<TaskEntity>> getAll();

  @Query("SELECT * FROM taskentity WHERE NOT completed")
  ListenableFuture<List<TaskEntity>> getAllIncomplete();

  // UPDATE
  @Update
  ListenableFuture<Void> update(TaskEntity... tasks);

  // DELETE
  @Delete
  ListenableFuture<Void> delete(TaskEntity... tasks);
}

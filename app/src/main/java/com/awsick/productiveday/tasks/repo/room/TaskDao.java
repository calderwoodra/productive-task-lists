package com.awsick.productiveday.tasks.repo.room;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.List;

@Dao
public interface TaskDao {

  @Query("SELECT * FROM taskentity")
  ListenableFuture<List<TaskEntity>> getAll();

  @Query("SELECT * FROM taskentity WHERE NOT completed")
  ListenableFuture<List<TaskEntity>> getAllIncomplete();

  @Insert
  ListenableFuture<Long> insert(TaskEntity task);

  @Delete
  ListenableFuture<Void> delete(TaskEntity task);
}

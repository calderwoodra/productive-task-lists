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

  @Query("SELECT * FROM taskentity WHERE NOT completed ORDER BY deadline ASC")
  ListenableFuture<List<TaskEntity>> getAllIncomplete();

  @Query(
      "SELECT * FROM taskentity WHERE directory IS :directoryId AND NOT completed ORDER BY"
          + " deadline ASC")
  ListenableFuture<List<TaskEntity>> getAllIncomplete(int directoryId);

  @Query("SELECT * FROM taskentity WHERE uid IS :id")
  ListenableFuture<TaskEntity> getTask(int id);

  /** Returns the list of tasks which the user needs to be notified about. */
  @Query(
      "SELECT * FROM taskentity WHERE "
          + "NOT notified "
          + "AND NOT completed "
          + "AND deadline IS NOT NULL "
          + "AND deadline <= :deadline")
  ListenableFuture<List<TaskEntity>> getNotifications(long deadline);

  // UPDATE
  @Update
  ListenableFuture<Void> update(TaskEntity... tasks);

  // DELETE
  @Delete
  ListenableFuture<Void> delete(TaskEntity... tasks);

  @Query(
      "SELECT * FROM taskentity WHERE "
          + "NOT notified "
          + "AND NOT completed "
          + "AND deadline IS NOT NULL "
          + "AND deadline > :deadline "
          + "ORDER BY deadline ASC "
          + "LIMIT 1")
  ListenableFuture<TaskEntity> getNextNotification(long deadline);
}

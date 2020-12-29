package com.awsick.productiveday.tasks.repo.room;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.google.common.util.concurrent.ListenableFuture;
import java.time.Clock;
import java.util.List;

@Dao
public abstract class TaskDao {

  // CREATE
  @Insert
  abstract ListenableFuture<Long> insertImpl(TaskEntity task);

  public ListenableFuture<Long> insert(Clock clock, TaskEntity task) {
    long now = clock.millis();
    task.createdMillis = now;
    task.updatedMillis = now;
    return insertImpl(task);
  }

  @Insert
  abstract ListenableFuture<List<Long>> insertImpl(TaskEntity... tasks);

  public ListenableFuture<List<Long>> insert(Clock clock, TaskEntity... tasks) {
    long now = clock.millis();
    for (TaskEntity task : tasks) {
      task.createdMillis = now;
      task.updatedMillis = now;
    }
    return insertImpl(tasks);
  }

  // READ
  @Query("SELECT * FROM taskentity")
  public abstract ListenableFuture<List<TaskEntity>> getAll();

  @Query("SELECT * FROM taskentity WHERE NOT completed ORDER BY updated_millis DESC")
  public abstract ListenableFuture<List<TaskEntity>> getAllIncomplete();

  @Query(
      "SELECT * FROM taskentity WHERE directory IS :directoryId AND NOT completed ORDER BY"
          + " deadline ASC")
  public abstract ListenableFuture<List<TaskEntity>> getAllIncomplete(int directoryId);

  @Query("SELECT * FROM taskentity WHERE uid IS :id")
  public abstract ListenableFuture<TaskEntity> getTask(int id);

  /** Returns the list of tasks which the user needs to be notified about. */
  @Query(
      "SELECT * FROM taskentity WHERE "
          + "NOT notified "
          + "AND NOT completed "
          + "AND deadline IS NOT NULL "
          + "AND deadline <= :deadline")
  public abstract ListenableFuture<List<TaskEntity>> getNotifications(long deadline);

  // UPDATE
  @Update
  abstract ListenableFuture<Void> updateImpl(TaskEntity... tasks);

  public ListenableFuture<Void> update(Clock clock, TaskEntity... tasks) {
    long now = clock.millis();
    for (TaskEntity task : tasks) {
      task.updatedMillis = now;
    }
    return updateImpl(tasks);
  }

  // DELETE
  @Delete
  public abstract ListenableFuture<Void> delete(TaskEntity... tasks);

  @Query(
      "SELECT * FROM taskentity WHERE "
          + "NOT notified "
          + "AND NOT completed "
          + "AND deadline IS NOT NULL "
          + "AND deadline > :deadline "
          + "ORDER BY deadline ASC "
          + "LIMIT 1")
  public abstract ListenableFuture<TaskEntity> getNextNotification(long deadline);
}

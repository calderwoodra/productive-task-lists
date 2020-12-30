package com.awsick.productiveday.tasks.repo.room;

import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import com.awsick.productiveday.tasks.models.Task;
import com.awsick.productiveday.tasks.models.TaskRepeatability;
import com.awsick.productiveday.tasks.models.TaskRepeatability.EndType;
import com.awsick.productiveday.tasks.models.TaskRepeatability.PeriodType;
import com.awsick.productiveday.tasks.models.TaskRepeatability.Weekly;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import java.time.Clock;

@Entity
public class TaskEntity {

  @PrimaryKey(autoGenerate = true)
  public int uid;

  @ColumnInfo(name = "created_millis")
  long createdMillis;

  @ColumnInfo(name = "updated_millis")
  long updatedMillis;

  @ColumnInfo(name = "title")
  public String title;

  @Nullable
  @ColumnInfo(name = "notes")
  public String notes;

  @Nullable
  @ColumnInfo(name = "deadline")
  public Long deadlineMillis;

  @ColumnInfo(name = "completed", defaultValue = "false")
  public boolean completed;

  @ColumnInfo(name = "task_type")
  public Task.Type taskType;

  // -1 comes from DirectoryRepo.ROOT_DIRECTORY_ID
  @ColumnInfo(name = "directory", defaultValue = "-1")
  public int directoryId;

  @ColumnInfo(name = "notified", defaultValue = "false")
  public boolean notified;

  @Nullable @Embedded public TaskRepeatabilityEntity repeatability;

  public static TaskEntity from(Task task) {
    TaskEntity entity = new TaskEntity();
    entity.uid = task.uid();
    entity.title = task.title();
    entity.notes = Strings.isNullOrEmpty(task.notes()) ? null : task.notes();
    entity.taskType = task.type();
    entity.deadlineMillis = task.deadlineMillis() == -1 ? null : task.deadlineMillis();
    entity.directoryId = task.directoryId();
    entity.completed = false;
    entity.repeatability = TaskRepeatabilityEntity.from(task.repeatability());
    entity.notified = task.notified();
    return entity;
  }

  public Task toTask(Clock clock) {
    return Task.builder()
        .setUid(uid)
        .setTitle(title)
        .setType(taskType)
        .setNotes(Strings.nullToEmpty(notes))
        .setDeadlineMillis(deadlineMillis == null ? -1 : deadlineMillis)
        .setRepeatability(null)
        .setDirectoryId(directoryId)
        .setNotified(notified)
        .setRepeatability(repeatability == null ? null : repeatability.toRepeatability())
        .setCompleted(completed)
        .setLastUpdated(updatedMillis)
        .setClock(clock)
        .build();
  }

  public static final class TaskRepeatabilityEntity {

    @ColumnInfo(name = "first_reminder")
    public long firstReminder;

    @ColumnInfo(name = "frequency")
    public int frequency;

    @ColumnInfo(name = "period_type")
    public PeriodType periodType;

    @Nullable @Embedded public TaskWeeklyRepeatabilityEntity weekly;

    @Nullable
    @ColumnInfo(name = "monthly")
    public Integer monthly;

    @ColumnInfo(name = "end_type")
    public EndType endType;

    @Nullable
    @ColumnInfo(name = "end_time_millis")
    public Long endTimeMillis;

    @Nullable
    @ColumnInfo(name = "end_after_n_times")
    public Integer endAfterNTimes;

    public static TaskRepeatabilityEntity from(@Nullable TaskRepeatability repeatability) {
      if (repeatability == null) {
        return null;
      }

      TaskRepeatabilityEntity entity = new TaskRepeatabilityEntity();
      entity.firstReminder = repeatability.firstReminder();
      entity.frequency = repeatability.frequency();
      entity.periodType = repeatability.periodType();
      entity.weekly = TaskWeeklyRepeatabilityEntity.from(repeatability.weekly());
      entity.monthly = repeatability.monthly().orNull();
      entity.endType = repeatability.endType();
      entity.endTimeMillis = repeatability.endOnTimeMillis().orNull();
      entity.endAfterNTimes = repeatability.endAfterNTimes().orNull();
      return entity;
    }

    public TaskRepeatability toRepeatability() {
      return TaskRepeatability.builder()
          .setFirstReminder(firstReminder)
          .setFrequency(frequency)
          .setPeriodType(periodType)
          .setWeekly(weekly == null ? Optional.absent() : weekly.toWeekly())
          .setMonthly(Optional.fromNullable(monthly))
          .setEndType(endType)
          .setEndOnTimeMillis(Optional.fromNullable(endTimeMillis))
          .setEndAfterNTimes(Optional.fromNullable(endAfterNTimes))
          .build();
    }

    public static final class TaskWeeklyRepeatabilityEntity {

      @ColumnInfo(name = "monday")
      public boolean m;

      @ColumnInfo(name = "tuesday")
      public boolean t;

      @ColumnInfo(name = "wednesday")
      public boolean w;

      @ColumnInfo(name = "thursday")
      public boolean r;

      @ColumnInfo(name = "friday")
      public boolean f;

      @ColumnInfo(name = "saturday")
      public boolean sa;

      @ColumnInfo(name = "sunday")
      public boolean su;

      public Optional<Weekly> toWeekly() {
        return Optional.of(
            Weekly.builder()
                .monday(m)
                .tuesday(t)
                .wednesday(w)
                .thursday(r)
                .friday(f)
                .saturday(sa)
                .sunday(su)
                .build());
      }

      public static TaskWeeklyRepeatabilityEntity from(Optional<Weekly> weekly) {
        if (!weekly.isPresent()) {
          return null;
        }

        TaskWeeklyRepeatabilityEntity entity = new TaskWeeklyRepeatabilityEntity();
        entity.m = weekly.get().monday();
        entity.t = weekly.get().tuesday();
        entity.w = weekly.get().wednesday();
        entity.r = weekly.get().thursday();
        entity.f = weekly.get().friday();
        entity.sa = weekly.get().saturday();
        entity.su = weekly.get().sunday();
        return entity;
      }
    }
  }
}

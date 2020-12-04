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
import com.google.common.base.Strings;

@Entity
public class TaskEntity {

  @PrimaryKey(autoGenerate = true)
  public int uid;

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
    return entity;
  }

  public Task toTask() {
    return Task.builder()
        .setUid(uid)
        .setTitle(title)
        .setType(taskType)
        .setNotes(Strings.nullToEmpty(notes))
        .setDeadlineMillis(deadlineMillis == null ? -1 : deadlineMillis)
        .setRepeatability(null)
        .setDirectoryId(directoryId)
        .setRepeatability(repeatability.toRepeatability())
        .build();
  }

  public static final class TaskRepeatabilityEntity {

    @ColumnInfo(name = "frequency")
    public int frequency;

    public static TaskRepeatabilityEntity from(@Nullable TaskRepeatability repeatability) {
      if (repeatability == null) {
        return null;
      }

      // TODO(allen): Add the remaining attributes
      TaskRepeatabilityEntity entity = new TaskRepeatabilityEntity();
      entity.frequency = repeatability.frequency();
      return entity;
    }

    public TaskRepeatability toRepeatability() {
      // TODO(allen): Convert the remaining attributes
      return TaskRepeatability.builder()
          .setFrequency(frequency)
          .setFirstReminder(0)
          .setPeriodType(PeriodType.DAILY)
          .setEndType(EndType.NEVER)
          .build();
    }
  }
}

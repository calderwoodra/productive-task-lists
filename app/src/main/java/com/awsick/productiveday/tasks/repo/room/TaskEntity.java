package com.awsick.productiveday.tasks.repo.room;

import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import com.awsick.productiveday.tasks.models.Task;
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

  public static TaskEntity from(Task task) {
    TaskEntity entity = new TaskEntity();
    entity.uid = task.uid();
    entity.title = task.title();
    entity.notes = Strings.isNullOrEmpty(task.notes()) ? null : task.notes();
    entity.taskType = task.type();
    entity.deadlineMillis = task.deadlineMillis() == -1 ? null : task.deadlineMillis();
    entity.completed = false;
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
        .setDirectoryId(-1)
        .build();
  }
}

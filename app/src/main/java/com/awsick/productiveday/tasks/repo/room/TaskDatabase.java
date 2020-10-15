package com.awsick.productiveday.tasks.repo.room;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(
    entities = {TaskEntity.class},
    version = 1)
public abstract class TaskDatabase extends RoomDatabase {

  public static final String NAME = "TASK_DATABASE";

  public abstract TaskDao taskDao();
}

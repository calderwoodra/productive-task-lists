package com.awsick.productiveday.tasks.repo.room;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@Database(
    entities = {TaskEntity.class},
    version = 2)
@TypeConverters(TaskDatabaseConverters.class)
public abstract class TaskDatabase extends RoomDatabase {

  public static final String NAME = "TASK_DATABASE";

  public abstract TaskDao taskDao();
}

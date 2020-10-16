package com.awsick.productiveday.tasks.repo.room;

import androidx.room.TypeConverter;
import com.awsick.productiveday.tasks.models.Task;

public final class TaskDatabaseConverters {

  @TypeConverter
  public static Task.Type toType(String value) {
    return Task.Type.valueOf(value);
  }

  @TypeConverter
  public String toTypeString(Task.Type type) {
    return type.toString();
  }
}

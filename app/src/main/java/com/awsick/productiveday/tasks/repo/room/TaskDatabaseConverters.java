package com.awsick.productiveday.tasks.repo.room;

import androidx.room.TypeConverter;
import com.awsick.productiveday.tasks.models.Task;
import com.awsick.productiveday.tasks.models.TaskRepeatability.EndType;
import com.awsick.productiveday.tasks.models.TaskRepeatability.PeriodType;

public final class TaskDatabaseConverters {

  @TypeConverter
  public static Task.Type toTaskType(String value) {
    return Task.Type.valueOf(value);
  }

  @TypeConverter
  public String toTaskTypeString(Task.Type type) {
    return type.toString();
  }

  @TypeConverter
  public static PeriodType toPeriodType(String value) {
    return PeriodType.valueOf(value);
  }

  @TypeConverter
  public String toPeriodTypeString(PeriodType type) {
    return type.toString();
  }

  @TypeConverter
  public static EndType toEndType(String value) {
    return EndType.valueOf(value);
  }

  @TypeConverter
  public String toEndTypeString(EndType type) {
    return type.toString();
  }
}

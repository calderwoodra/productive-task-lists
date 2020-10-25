package com.awsick.productiveday.tasks.repo;

import androidx.lifecycle.LiveData;
import com.awsick.productiveday.network.RequestStatus;
import com.awsick.productiveday.tasks.models.Task;
import com.google.common.collect.ImmutableList;

public interface TasksRepo {

  /** Returns all the incomplete tasks. */
  LiveData<RequestStatus<ImmutableList<Task>>> getIncompleteTasks();

  /** Returns all the incomplete tasks in the given directory. */
  LiveData<RequestStatus<ImmutableList<Task>>> getIncompleteTasks(int directoryId);

  void createTask(Task task);

  void updateTask(Task existingTask, Task newTask);

  void deleteTask(Task task);

  void markTaskCompleted(Task task);
}

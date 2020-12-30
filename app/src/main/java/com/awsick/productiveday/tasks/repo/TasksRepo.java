package com.awsick.productiveday.tasks.repo;

import androidx.lifecycle.LiveData;
import com.awsick.productiveday.network.RequestStatus;
import com.awsick.productiveday.tasks.models.Task;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.ListenableFuture;

public interface TasksRepo {

  /** Returns all tasks (complete, incomplete, etc.). */
  LiveData<RequestStatus<ImmutableList<Task>>> getAllTasks();

  /** Returns all the incomplete tasks. */
  LiveData<RequestStatus<ImmutableList<Task>>> getIncompleteTasks();

  /** Returns all the incomplete tasks in the given directory. */
  LiveData<RequestStatus<ImmutableList<Task>>> getIncompleteTasks(int directoryId);

  void createTask(Task task);

  void createTasks(ImmutableList<Task> tasks);

  void updateTask(Task existingTask, Task newTask);

  void deleteTask(Task task);

  void markTaskCompleted(Task task);

  void markTaskIncomplete(Task task);

  ListenableFuture<ImmutableList<Task>> getTasksToBeNotified();

  ListenableFuture<Optional<Task>> getUpcomingTaskToBeNotified();

  ListenableFuture<Void> markTasksAsNotified(ImmutableList<Task> tasks);
}

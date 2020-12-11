package com.awsick.productiveday.tasks.repo;

import static com.awsick.productiveday.common.utils.ImmutableUtils.toImmutableList;

import android.content.Context;
import androidx.lifecycle.LiveData;
import com.awsick.productiveday.common.utils.Assert;
import com.awsick.productiveday.network.RequestStatus;
import com.awsick.productiveday.network.RequestStatus.Status;
import com.awsick.productiveday.network.util.RequestStatusLiveData;
import com.awsick.productiveday.network.util.RequestStatusUtils;
import com.awsick.productiveday.tasks.models.Task;
import com.awsick.productiveday.tasks.repo.room.TaskDatabase;
import com.awsick.productiveday.tasks.repo.room.TaskEntity;
import com.awsick.productiveday.tasks.scheduling.TaskSchedulerWorker;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import dagger.hilt.android.qualifiers.ApplicationContext;
import java.util.HashMap;
import java.util.concurrent.Executor;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;
import org.jetbrains.annotations.NotNull;

@Singleton
class TasksRepoImpl implements TasksRepo {

  private final Context context;
  private final TaskDatabase taskDatabase;
  private final RequestStatusLiveData<ImmutableList<Task>> tasks = new RequestStatusLiveData<>();
  private final HashMap<Integer, RequestStatusLiveData<ImmutableList<Task>>> directoryTasksMap =
      new HashMap<>();
  private final Executor executor;

  @Inject
  TasksRepoImpl(@ApplicationContext Context context, TaskDatabase taskDatabase, Executor executor) {
    this.context = context;
    this.taskDatabase = taskDatabase;
    this.executor = executor;
  }

  @Override
  public LiveData<RequestStatus<ImmutableList<Task>>> getIncompleteTasks() {
    if (tasks.getValue().status == Status.INITIAL) {
      refreshTasks();
    }
    return tasks;
  }

  @Override
  public LiveData<RequestStatus<ImmutableList<Task>>> getIncompleteTasks(int directoryId) {
    if (directoryTasksMap.containsKey(directoryId)) {
      return directoryTasksMap.get(directoryId);
    }

    RequestStatusLiveData<ImmutableList<Task>> tasks = new RequestStatusLiveData<>();
    tasks.setValue(RequestStatus.pending());
    Futures.addCallback(
        taskDatabase.taskDao().getAllIncomplete(directoryId),
        RequestStatusUtils.futureCallback(
            tasks,
            result ->
                tasks.setValue(
                    RequestStatus.success(
                        result.stream().map(TaskEntity::toTask).collect(toImmutableList())))),
        executor);
    directoryTasksMap.put(directoryId, tasks);
    return tasks;
  }

  @Override
  public void createTask(Task task) {
    // TODO(allen): Consider adding the task optimistically
    tasks.setValue(RequestStatus.pending());
    Futures.addCallback(
        taskDatabase.taskDao().insert(TaskEntity.from(task)),
        new FutureCallback<Long>() {
          @Override
          public void onSuccess(Long uid) {
            refreshTasks();
            refreshDirectoryTasks(task.directoryId());
            TaskSchedulerWorker.updateNextReminder(context);
          }

          @Override
          public void onFailure(@NotNull Throwable throwable) {
            // TODO(allen): Push a "failed to save" event to the front-end
            refreshTasks();
          }
        },
        executor);
  }

  private void refreshTasks() {
    if (tasks.getValue().status != Status.SUCCESS) {
      tasks.setValue(RequestStatus.pending());
    }

    Futures.addCallback(
        taskDatabase.taskDao().getAllIncomplete(),
        RequestStatusUtils.futureCallback(
            tasks,
            result ->
                tasks.setValue(
                    RequestStatus.success(
                        result.stream().map(TaskEntity::toTask).collect(toImmutableList())))),
        executor);
  }

  private void refreshDirectoryTasks(int directoryId) {
    RequestStatusLiveData<ImmutableList<Task>> tasks;
    if (directoryTasksMap.containsKey(directoryId)) {
      tasks = directoryTasksMap.get(directoryId);
    } else {
      tasks = new RequestStatusLiveData<>();
    }

    tasks.setValue(RequestStatus.pending());
    Futures.addCallback(
        taskDatabase.taskDao().getAllIncomplete(directoryId),
        RequestStatusUtils.futureCallback(
            tasks,
            result ->
                tasks.setValue(
                    RequestStatus.success(
                        result.stream().map(TaskEntity::toTask).collect(toImmutableList())))),
        executor);
  }

  @Override
  public void updateTask(Task existingTask, Task newTask) {
    // TODO(allen): Consider adding the task optimistically
    tasks.setValue(RequestStatus.pending());
    Futures.addCallback(
        taskDatabase.taskDao().update(TaskEntity.from(newTask)),
        new FutureCallback<Void>() {
          @Override
          public void onSuccess(Void voidd) {
            refreshTasks();
            refreshDirectoryTasks(newTask.directoryId());
            if (existingTask.directoryId() != newTask.directoryId()) {
              refreshDirectoryTasks(existingTask.directoryId());
            }
            TaskSchedulerWorker.updateNextReminder(context);
          }

          @Override
          public void onFailure(@NotNull Throwable throwable) {
            // TODO(allen): Push a "failed to save" event to the front-end
            refreshTasks();
          }
        },
        executor);
  }

  @Override
  public void deleteTask(Task task) {
    // TODO(allen): implement
  }

  @Override
  public void markTaskCompleted(Task task) {
    // Update task list to exclude compelted task
    tasks.setValue(
        RequestStatus.success(
            tasks.getValue().getResult().stream()
                .filter(t -> t.uid() != task.uid())
                .collect(toImmutableList())));

    // Update directory list too, if it exists
    if (directoryTasksMap.containsKey(task.directoryId())) {
      RequestStatusLiveData<ImmutableList<Task>> directoryTasks =
          directoryTasksMap.get(task.directoryId());
      directoryTasks.setValue(
          RequestStatus.success(
              directoryTasks.getValue().getResult().stream()
                  .filter(t -> t.uid() != task.uid())
                  .collect(toImmutableList())));
    }

    TaskEntity entity = TaskEntity.from(task);
    entity.completed = true;
    Futures.addCallback(
        taskDatabase.taskDao().update(entity),
        new FutureCallback<Void>() {
          @Override
          public void onSuccess(@NullableDecl Void result) {
            // No-op
          }

          @Override
          public void onFailure(Throwable throwable) {
            // TODO(allen): Push a "failed to mark completed" event to front-end
            refreshTasks();
          }
        },
        executor);
  }

  @Override
  public ListenableFuture<ImmutableList<Task>> getTasksToBeNotified() {
    Assert.isWorkerThread("Cannot perform disk I/O on the main thread");
    return Futures.transform(
        taskDatabase.taskDao().getNotifications(System.currentTimeMillis()),
        tasks -> tasks.stream().map(TaskEntity::toTask).collect(toImmutableList()),
        executor);
  }

  @Override
  public ListenableFuture<Optional<Task>> getUpcomingTaskToBeNotified() {
    return Futures.transform(
        taskDatabase.taskDao().getNextNotification(System.currentTimeMillis()),
        taskEntity -> taskEntity == null ? Optional.absent() : Optional.of(taskEntity.toTask()),
        executor);
  }

  @Override
  public ListenableFuture<Void> markTasksAsNotified(ImmutableList<Task> tasks) {
    return taskDatabase
        .taskDao()
        .update(
            tasks.stream()
                .map(
                    task -> {
                      // TODO(allen): Check for repeatability
                      TaskEntity entity = TaskEntity.from(task);
                      entity.notified = true;
                      return entity;
                    })
                .toArray(TaskEntity[]::new));
  }
}

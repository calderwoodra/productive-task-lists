package com.awsick.productiveday.tasks.repo;

import static com.awsick.productiveday.common.utils.ImmutableUtils.toImmutableList;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import com.awsick.productiveday.network.RequestStatus;
import com.awsick.productiveday.network.RequestStatus.Status;
import com.awsick.productiveday.network.util.RequestStatusLiveData;
import com.awsick.productiveday.network.util.RequestStatusUtils;
import com.awsick.productiveday.tasks.models.Task;
import com.awsick.productiveday.tasks.repo.room.TaskDatabase;
import com.awsick.productiveday.tasks.repo.room.TaskEntity;
import com.awsick.productiveday.tasks.scheduling.SchedulingUtils;
import com.awsick.productiveday.tasks.scheduling.notifications.NotificationsRepo;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;
import org.jetbrains.annotations.NotNull;

@Singleton
class TasksRepoImpl implements TasksRepo {

  private final TaskDatabase taskDatabase;
  private final NotificationsRepo notificationsRepo;
  private final RequestStatusLiveData<ImmutableList<Task>> tasks = new RequestStatusLiveData<>();
  private final HashMap<Integer, RequestStatusLiveData<ImmutableList<Task>>> directoryTasksMap =
      new HashMap<>();
  private final Executor executor;

  @Inject
  TasksRepoImpl(TaskDatabase taskDatabase, NotificationsRepo notificationsRepo, Executor executor) {
    this.taskDatabase = taskDatabase;
    this.notificationsRepo = notificationsRepo;
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
            notificationsRepo.notifyUser(TasksRepoImpl.this);
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
  public void createTasks(ImmutableList<Task> tasks) {
    this.tasks.setValue(RequestStatus.pending());
    Futures.addCallback(
        taskDatabase
            .taskDao()
            .insert(tasks.stream().map(TaskEntity::from).toArray(TaskEntity[]::new)),
        new FutureCallback<List<Long>>() {
          @Override
          public void onSuccess(@NullableDecl List<Long> result) {
            refreshTasks();
            Set<Integer> directoryIds =
                tasks.stream().map(Task::directoryId).collect(Collectors.toSet());
            for (int directory : directoryIds) {
              refreshDirectoryTasks(directory);
            }
            notificationsRepo.notifyUser(TasksRepoImpl.this);
          }

          @Override
          public void onFailure(@NonNull Throwable throwable) {
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
            notificationsRepo.notifyUser(TasksRepoImpl.this);
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
    tasks.setValue(RequestStatus.pending());

    TaskEntity entity = TaskEntity.from(task);
    Optional<Long> nextDeadline = SchedulingUtils.getNextDeadline(task);
    if (!nextDeadline.isPresent()) {
      entity.completed = true;
    } else {
      entity.deadlineMillis = nextDeadline.get();
      entity.notified = false;
    }

    ListenableFuture<Void> updateTask = taskDatabase.taskDao().update(entity);

    Futures.addCallback(
        updateTask,
        new FutureCallback<Void>() {
          @Override
          public void onSuccess(@NullableDecl Void result) {
            refreshTasks();
            refreshDirectoryTasks(task.directoryId());
            notificationsRepo.notifyUser(TasksRepoImpl.this);
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
    // Assert.isWorkerThread("Cannot perform disk I/O on the main thread");
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
                      TaskEntity entity = TaskEntity.from(task);
                      entity.notified = true;
                      return entity;
                    })
                .toArray(TaskEntity[]::new));
  }
}

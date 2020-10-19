package com.awsick.productiveday.tasks.repo;

import static com.awsick.productiveday.common.utils.ImmutableUtils.toImmutableList;

import androidx.lifecycle.LiveData;
import com.awsick.productiveday.network.RequestStatus;
import com.awsick.productiveday.network.RequestStatus.Status;
import com.awsick.productiveday.network.util.RequestStatusLiveData;
import com.awsick.productiveday.network.util.RequestStatusUtils;
import com.awsick.productiveday.tasks.models.Task;
import com.awsick.productiveday.tasks.repo.room.TaskDatabase;
import com.awsick.productiveday.tasks.repo.room.TaskEntity;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import java.util.concurrent.Executor;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;
import org.jetbrains.annotations.NotNull;

@Singleton
class TasksRepoImpl implements TasksRepo {

  private final TaskDatabase tasksDatabase;
  private final RequestStatusLiveData<ImmutableList<Task>> tasks = new RequestStatusLiveData<>();
  private final Executor executor;

  @Inject
  TasksRepoImpl(TaskDatabase taskDatabase, Executor executor) {
    tasksDatabase = taskDatabase;
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
    // TODO(allen): Cache these LiveData in a map
    RequestStatusLiveData<ImmutableList<Task>> tasks = new RequestStatusLiveData<>();
    tasks.setValue(RequestStatus.pending());
    Futures.addCallback(
        tasksDatabase.taskDao().getAllIncomplete(directoryId),
        RequestStatusUtils.futureCallback(
            tasks,
            result ->
                tasks.setValue(
                    RequestStatus.success(
                        result.stream().map(TaskEntity::toTask).collect(toImmutableList())))),
        executor);
    return tasks;
  }

  @Override
  public void createTask(Task task) {
    // TODO(allen): Consider adding the task optimistically
    tasks.setValue(RequestStatus.pending());
    Futures.addCallback(
        tasksDatabase.taskDao().insert(TaskEntity.from(task)),
        new FutureCallback<Long>() {
          @Override
          public void onSuccess(Long uid) {
            refreshTasks();
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
        tasksDatabase.taskDao().getAllIncomplete(),
        RequestStatusUtils.futureCallback(
            tasks,
            result ->
                tasks.setValue(
                    RequestStatus.success(
                        result.stream().map(TaskEntity::toTask).collect(toImmutableList())))),
        executor);
  }

  @Override
  public void updateTask(Task task) {
    // TODO(allen): implement
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

    TaskEntity entity = TaskEntity.from(task);
    entity.completed = true;
    Futures.addCallback(
        tasksDatabase.taskDao().update(entity),
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
}

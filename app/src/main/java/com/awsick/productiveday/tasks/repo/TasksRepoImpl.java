package com.awsick.productiveday.tasks.repo;

import android.util.Log;
import androidx.lifecycle.LiveData;
import com.awsick.productiveday.network.RequestStatus;
import com.awsick.productiveday.network.RequestStatus.Status;
import com.awsick.productiveday.network.util.RequestStatusLiveData;
import com.awsick.productiveday.tasks.models.Task;
import com.awsick.productiveday.tasks.repo.room.TaskDatabase;
import com.awsick.productiveday.tasks.repo.room.TaskEntity;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Singleton;
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
            // TODO(allen): Consider pushing a "failed to save" event to the front-end
            refreshTasks();
          }
        },
        executor);
  }

  private void refreshTasks() {
    if (tasks.getValue().status != Status.SUCCESS) {
      Log.i("###", "pending");
      tasks.setValue(RequestStatus.pending());
    }

    Futures.addCallback(
        tasksDatabase.taskDao().getAllIncomplete(),
        new FutureCallback<List<TaskEntity>>() {
          @Override
          public void onSuccess(List<TaskEntity> result) {
            Log.i("###", "Success");
            tasks.setValue(
                RequestStatus.success(
                    ImmutableList.copyOf(
                        result.stream().map(TaskEntity::toTask).collect(Collectors.toList()))));
            Log.i("###", "Success 2");
          }

          @Override
          public void onFailure(@NotNull Throwable throwable) {
            Log.i("###", "failure");
            tasks.setValue(RequestStatus.error(throwable));
          }
        },
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
}

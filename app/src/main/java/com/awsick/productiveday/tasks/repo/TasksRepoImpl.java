package com.awsick.productiveday.tasks.repo;

import androidx.lifecycle.LiveData;
import com.awsick.productiveday.network.RequestStatus;
import com.awsick.productiveday.network.RequestStatus.Status;
import com.awsick.productiveday.network.util.RequestStatusLiveData;
import com.awsick.productiveday.tasks.models.Task;
import com.google.common.collect.ImmutableList;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
class TasksRepoImpl implements TasksRepo {

  private final RequestStatusLiveData<ImmutableList<Task>> tasks = new RequestStatusLiveData<>();

  @Inject
  TasksRepoImpl() {}

  @Override
  public LiveData<RequestStatus<ImmutableList<Task>>> getTasks() {
    if (tasks.getValue().status == Status.INITIAL) {
      refreshTasks();
    }
    return tasks;
  }

  @Override
  public void createTask(Task task) {
    // TODO(allen): Store task in DB and refetch
    tasks.setValue(RequestStatus.success(
        ImmutableList.<Task>builder()
            .add(task)
            .addAll(tasks.getValue().getResult())
            .build()));
  }

  private void refreshTasks() {
    // TODO(allen): Read tasks from DB
    tasks.setValue(RequestStatus.success(ImmutableList.<Task>builder()
        .add(new Task())
        .add(new Task())
        .add(new Task())
        .build()));
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

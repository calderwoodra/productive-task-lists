package com.awsick.productiveday.tasks.repo;

import androidx.lifecycle.LiveData;
import com.awsick.productiveday.network.RequestStatus;
import com.awsick.productiveday.network.util.RequestStatusLiveData;
import com.awsick.productiveday.tasks.models.Task;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
class TasksRepoImpl implements TasksRepo {

  private final RequestStatusLiveData<List<Task>> tasks = new RequestStatusLiveData<>();

  @Inject
  TasksRepoImpl() {}

  @Override
  public LiveData<RequestStatus<List<Task>>> getTasks() {
    return tasks;
  }

  @Override
  public void createTask(Task task) {

  }

  @Override
  public void updateTask(Task task) {

  }

  @Override
  public void deleteTask(Task task) {

  }
}

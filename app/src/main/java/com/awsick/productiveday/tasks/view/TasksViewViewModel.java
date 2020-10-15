package com.awsick.productiveday.tasks.view;

import androidx.hilt.lifecycle.ViewModelInject;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import com.awsick.productiveday.network.RequestStatus;
import com.awsick.productiveday.tasks.models.Task;
import com.awsick.productiveday.tasks.repo.TasksRepo;
import com.google.common.collect.ImmutableList;

public final class TasksViewViewModel extends ViewModel {

  private final TasksRepo tasksRepo;

  @ViewModelInject
  TasksViewViewModel(TasksRepo tasksRepo) {
    this.tasksRepo = tasksRepo;
  }

  public LiveData<RequestStatus<ImmutableList<Task>>> getTasks() {
    return tasksRepo.getIncompleteTasks();
  }
}

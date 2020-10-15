package com.awsick.productiveday.tasks.repo;

import androidx.lifecycle.LiveData;
import com.awsick.productiveday.network.RequestStatus;
import com.awsick.productiveday.tasks.models.Task;
import com.google.common.collect.ImmutableList;

public interface TasksRepo {

  LiveData<RequestStatus<ImmutableList<Task>>> getIncompleteTasks();

  void createTask(Task task);

  void updateTask(Task task);

  void deleteTask(Task task);
}

package com.awsick.productiveday.tasks.repo;

import androidx.lifecycle.LiveData;
import com.awsick.productiveday.network.RequestStatus;
import com.awsick.productiveday.tasks.models.Task;
import java.util.List;

public interface TasksRepo {

  LiveData<RequestStatus<List<Task>>> getTasks();

  void createTask(Task task);

  void updateTask(Task task);

  void deleteTask(Task task);
}

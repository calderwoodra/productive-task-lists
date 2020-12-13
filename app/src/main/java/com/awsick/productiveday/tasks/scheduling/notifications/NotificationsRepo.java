package com.awsick.productiveday.tasks.scheduling.notifications;

import com.awsick.productiveday.tasks.repo.TasksRepo;

public interface NotificationsRepo {

  void scheduleNextReminder(TasksRepo tasksRepo);

  void notifyUser(TasksRepo tasksRepo);
}

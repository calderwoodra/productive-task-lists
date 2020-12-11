package com.awsick.productiveday.tasks.scheduling;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.hilt.Assisted;
import androidx.hilt.work.WorkerInject;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.OneTimeWorkRequest.Builder;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.awsick.productiveday.tasks.models.Task;
import com.awsick.productiveday.tasks.repo.TasksRepo;
import com.google.common.base.Optional;
import java.util.concurrent.ExecutionException;

/** Worker for scheduling the next upcoming task's alarm. */
public final class TaskSchedulerWorker extends Worker {

  private static final String TAG = "TaskReminderScheduler";

  private final Context context;
  private final TasksRepo tasksRepo;

  public static void updateNextReminder(Context context) {
    OneTimeWorkRequest request = new Builder(TaskSchedulerWorker.class).addTag(TAG).build();
    WorkManager.getInstance(context).enqueueUniqueWork(TAG, ExistingWorkPolicy.KEEP, request);
  }

  @WorkerInject
  public TaskSchedulerWorker(
      @Assisted Context context, @Assisted WorkerParameters params, TasksRepo tasksRepo) {
    super(context, params);
    this.context = context;
    this.tasksRepo = tasksRepo;
  }

  @NonNull
  @Override
  public Result doWork() {
    try {
      Optional<Task> task = tasksRepo.getUpcomingTaskToBeNotified().get();
      if (task.isPresent()) {
        TaskReminderAlarmReceiver.schedule(context, task.get());
      }
      return Result.success();

    } catch (ExecutionException | InterruptedException e) {
      Log.e(TAG, "Failed to schedule", e);
      return Result.retry();
    }
  }
}

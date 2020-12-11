package com.awsick.productiveday.tasks.scheduling;

import static com.awsick.productiveday.common.utils.ImmutableUtils.toImmutableList;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.media.RingtoneManager;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.hilt.Assisted;
import androidx.hilt.work.WorkerInject;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.OneTimeWorkRequest.Builder;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.awsick.productiveday.R;
import com.awsick.productiveday.common.notifications.NotificationConstants;
import com.awsick.productiveday.tasks.create.TaskCreateActivity;
import com.awsick.productiveday.tasks.models.Task;
import com.awsick.productiveday.tasks.repo.TasksRepo;
import com.google.common.collect.ImmutableList;
import java.util.concurrent.ExecutionException;

/** Creates the reminders' notifications for the user and displays them. */
public final class TaskNotifierWorker extends Worker {

  private static final String TAG = "TaskNotifierWorker";

  private final Context context;
  private final TasksRepo tasksRepo;

  public static void notifyUser(Context context) {
    OneTimeWorkRequest request = new Builder(TaskNotifierWorker.class).addTag(TAG).build();
    WorkManager.getInstance(context).enqueueUniqueWork(TAG, ExistingWorkPolicy.KEEP, request);
  }

  @WorkerInject
  public TaskNotifierWorker(
      @Assisted Context context, @Assisted WorkerParameters params, TasksRepo tasksRepo) {
    super(context, params);
    this.context = context;
    this.tasksRepo = tasksRepo;
  }

  @NonNull
  @Override
  public Result doWork() {
    ImmutableList<Task> tasks;
    try {
      tasks = tasksRepo.getTasksToBeNotified().get();
    } catch (ExecutionException | InterruptedException e) {
      Log.e(TAG, "Failed to find tasks to be notified");
      TaskSchedulerWorker.updateNextReminder(context);
      return Result.failure();
    }

    int groupAlertBehavior =
        tasks.size() > 1
            ? NotificationCompat.GROUP_ALERT_CHILDREN
            : NotificationCompat.GROUP_ALERT_ALL;

    ImmutableList<Task> sortedTasks =
        tasks.stream()
            .sorted((t1, t2) -> Long.compare(t1.deadlineMillis(), t2.deadlineMillis()))
            .collect(toImmutableList());

    for (Task task : sortedTasks) {
      Notification notification =
          new NotificationCompat.Builder(context, NotificationConstants.REMINDERS_CHANNEL_ID)
              .setGroup(NotificationConstants.GROUP_KEY_REMINDERS)
              .setGroupSummary(true)
              .setGroupAlertBehavior(groupAlertBehavior)
              .setSmallIcon(R.mipmap.ic_launcher_round)
              .setContentTitle(task.title())
              .setContentIntent(createTaskIntent(context, task))
              .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
              .build();
      NotificationManagerCompat.from(context).notify(task.uid(), notification);
    }

    try {
      tasksRepo.markTasksAsNotified(tasks).get();
    } catch (ExecutionException | InterruptedException e) {
      Log.e(TAG, "Failed to mark tasks as notified");
      TaskSchedulerWorker.updateNextReminder(context);
      return Result.failure();
    }
    TaskSchedulerWorker.updateNextReminder(context);
    return Result.success();
  }

  private static PendingIntent createTaskIntent(Context context, Task task) {
    return PendingIntent.getActivity(
        context, 0, TaskCreateActivity.create(context, task), PendingIntent.FLAG_UPDATE_CURRENT);
  }
}

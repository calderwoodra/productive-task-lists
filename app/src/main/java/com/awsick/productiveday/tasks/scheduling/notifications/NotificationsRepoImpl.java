package com.awsick.productiveday.tasks.scheduling.notifications;

import static com.awsick.productiveday.common.utils.ImmutableUtils.toImmutableList;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.media.RingtoneManager;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import com.awsick.productiveday.R;
import com.awsick.productiveday.common.notifications.NotificationConstants;
import com.awsick.productiveday.common.utils.Assert;
import com.awsick.productiveday.tasks.create.TaskCreateActivity;
import com.awsick.productiveday.tasks.models.Task;
import com.awsick.productiveday.tasks.models.Task.Type;
import com.awsick.productiveday.tasks.repo.TasksRepo;
import com.awsick.productiveday.tasks.scheduling.TaskReminderAlarmReceiver;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import dagger.hilt.android.qualifiers.ApplicationContext;
import java.util.concurrent.Executor;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.jetbrains.annotations.NotNull;

@Singleton
final class NotificationsRepoImpl implements NotificationsRepo {

  private final Context context;
  private final Executor executor;

  @Inject
  NotificationsRepoImpl(@ApplicationContext Context context, Executor executor) {
    this.context = context;
    this.executor = executor;
  }

  @Override
  public void scheduleNextReminder(TasksRepo tasksRepo) {
    Futures.addCallback(
        tasksRepo.getUpcomingTaskToBeNotified(),
        new FutureCallback<Optional<Task>>() {
          @Override
          public void onSuccess(Optional<Task> task) {
            if (task.isPresent()) {
              scheduleAlarm(context, tasksRepo, task.get());
            }
          }

          @Override
          public void onFailure(@NotNull Throwable throwable) {
            // TODO(allen): Consider scheduling this to try again later
            // TODO(allen): log an error
          }
        },
        executor);
  }

  private void scheduleAlarm(Context context, TasksRepo tasksRepo, Task task) {
    // Validate the task
    Assert.checkArgument(task.uid() != 0);
    Assert.checkArgument(
        task.type() == Type.REMINDER || task.type() == Type.DEADLINE,
        "Unsupported task type: " + task.type());
    Assert.checkState(task.deadlineMillis() != -1, "Cannot schedule task with no deadline");

    // Task deadline has already passed, notify the user immediately
    if (task.deadlineMillis() < System.currentTimeMillis()) {
      notifyUser(tasksRepo, ImmutableList.of(task));
      return;
    }
    TaskReminderAlarmReceiver.schedule(context, task.deadlineMillis());
  }

  @Override
  public void notifyUser(TasksRepo tasksRepo) {
    Futures.addCallback(
        Futures.transformAsync(
            tasksRepo.getTasksToBeNotified(), tasks -> notifyUser(tasksRepo, tasks), executor),
        new FutureCallback<Void>() {
          @Override
          public void onSuccess(Void result) {
            scheduleNextReminder(tasksRepo);
          }

          @Override
          public void onFailure(@NotNull Throwable throwable) {
            // TODO(allen): Consider scheduling this to try again later
            // TODO(allen): log an error
          }
        },
        executor);
  }

  private ListenableFuture<Void> notifyUser(TasksRepo tasksRepo, ImmutableList<Task> tasks) {
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
    return tasksRepo.markTasksAsNotified(tasks);
  }

  private static PendingIntent createTaskIntent(Context context, Task task) {
    return PendingIntent.getActivity(
        context, 0, TaskCreateActivity.create(context, task), PendingIntent.FLAG_UPDATE_CURRENT);
  }
}

package com.awsick.productiveday.tasks.scheduling;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.awsick.productiveday.common.utils.Assert;
import com.awsick.productiveday.tasks.models.Task;
import com.awsick.productiveday.tasks.models.Task.Type;
import com.awsick.productiveday.tasks.repo.TasksRepo;
import dagger.hilt.android.AndroidEntryPoint;
import javax.inject.Inject;

/**
 * Schedules alarms which in turn schedule a job for showing reminders' notifications.
 *
 * @see #schedule(Context, Task) to schedule a task notification.
 */
@AndroidEntryPoint
public class TaskReminderAlarmReceiver extends BroadcastReceiver {

  private static final String REMINDER_ACTION = "task_reminder_alarm_receiver";
  private static final int TASK_REMINDER_REQUEST_CODE = 1;

  @Inject TasksRepo tasksRepo;

  static void schedule(Context context, Task task) {
    // Validate the task
    Assert.checkArgument(task.uid() != 0);
    Assert.checkArgument(
        task.type() == Type.REMINDER || task.type() == Type.DEADLINE,
        "Unsupported task type: " + task.type());
    Assert.checkState(task.deadlineMillis() != -1, "Cannot schedule task with no deadline");

    if (task.deadlineMillis() < System.currentTimeMillis()) {
      TaskNotifierWorker.notifyUser(context);
      return;
    }

    // Build the intent that will trigger this broadcast receiver
    Intent alarmIntent = new Intent(context, TaskReminderAlarmReceiver.class);
    alarmIntent.setAction(REMINDER_ACTION);
    PendingIntent pendingAlarmIntent =
        PendingIntent.getBroadcast(
            context, TASK_REMINDER_REQUEST_CODE, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);

    // Schedule the intent with the alarm manager
    AlarmManager alarmManager = context.getSystemService(AlarmManager.class);
    alarmManager.setExactAndAllowWhileIdle(
        AlarmManager.RTC_WAKEUP, task.deadlineMillis(), pendingAlarmIntent);
  }

  @Override
  public void onReceive(Context context, Intent intent) {
    Assert.checkArgument(intent.getAction().equals(REMINDER_ACTION));
    TaskNotifierWorker.notifyUser(context);
  }
}

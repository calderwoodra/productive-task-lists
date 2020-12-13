package com.awsick.productiveday.tasks.scheduling;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.awsick.productiveday.common.utils.Assert;
import com.awsick.productiveday.tasks.repo.TasksRepo;
import com.awsick.productiveday.tasks.scheduling.notifications.NotificationsRepo;
import dagger.hilt.android.AndroidEntryPoint;
import java.util.Calendar;
import javax.inject.Inject;

/**
 * Schedules alarms which in turn schedule a job for showing reminders' notifications.
 *
 * @see #schedule(Context, long) to schedule a task notification.
 */
@AndroidEntryPoint
public class TaskReminderAlarmReceiver extends BroadcastReceiver {

  private static final String TAG = "TaskReminderAlarmRcvr";
  private static final String REMINDER_ACTION = "task_reminder_alarm_receiver";
  private static final int TASK_REMINDER_REQUEST_CODE = 1;

  @Inject TasksRepo tasksRepo;
  @Inject NotificationsRepo notificationsRepo;

  public static void schedule(Context context, long taskDeadlineMillis) {
    Log.i(TAG, "Scheduling next reminder");
    Calendar calendar = Calendar.getInstance();
    calendar.setTimeInMillis(taskDeadlineMillis);
    // Build the intent that will trigger this broadcast receiver
    Intent alarmIntent = new Intent(context, TaskReminderAlarmReceiver.class);
    alarmIntent.setAction(REMINDER_ACTION);
    PendingIntent pendingAlarmIntent =
        PendingIntent.getBroadcast(
            context, TASK_REMINDER_REQUEST_CODE, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);

    // Schedule the intent with the alarm manager
    AlarmManager alarmManager = context.getSystemService(AlarmManager.class);
    alarmManager.setExactAndAllowWhileIdle(
        AlarmManager.RTC_WAKEUP, taskDeadlineMillis, pendingAlarmIntent);
  }

  @Override
  public void onReceive(Context context, Intent intent) {
    Assert.checkArgument(intent.getAction().equals(REMINDER_ACTION));
    notificationsRepo.notifyUser(tasksRepo);
  }
}

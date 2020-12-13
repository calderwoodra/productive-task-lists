package com.awsick.productiveday.tasks.scheduling;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.awsick.productiveday.common.utils.Assert;
import com.awsick.productiveday.tasks.repo.TasksRepo;
import com.awsick.productiveday.tasks.scheduling.notifications.NotificationsRepo;
import dagger.hilt.android.AndroidEntryPoint;
import javax.inject.Inject;

/** Listens for the ON_BOOT_COMPLETED and reschedules the next {@link android.app.AlarmManager}. */
@AndroidEntryPoint
public class OnBootReceiver extends BroadcastReceiver {

  @Inject TasksRepo tasksRepo;
  @Inject NotificationsRepo notificationsRepo;

  @Override
  public void onReceive(Context context, Intent intent) {
    Assert.checkArgument(intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED));
    notificationsRepo.scheduleNextReminder(tasksRepo);
  }
}

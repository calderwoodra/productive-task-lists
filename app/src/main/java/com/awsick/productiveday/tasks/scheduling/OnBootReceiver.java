package com.awsick.productiveday.tasks.scheduling;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.awsick.productiveday.common.utils.Assert;
import dagger.hilt.android.AndroidEntryPoint;

/** Listens for the ON_BOOT_COMPLETED and reschedules the next {@link android.app.AlarmManager}. */
@AndroidEntryPoint
public class OnBootReceiver extends BroadcastReceiver {

  @Override
  public void onReceive(Context context, Intent intent) {
    Assert.checkArgument(intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED));
    TaskSchedulerWorker.updateNextReminder(context);
  }
}

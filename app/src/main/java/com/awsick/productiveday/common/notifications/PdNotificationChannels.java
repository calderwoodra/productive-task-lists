package com.awsick.productiveday.common.notifications;

import static com.awsick.productiveday.common.notifications.NotificationConstants.REMINDERS_CHANNEL_GROUP_ID;
import static com.awsick.productiveday.common.notifications.NotificationConstants.REMINDERS_CHANNEL_ID;

import android.app.NotificationChannel;
import android.app.NotificationChannelGroup;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

/** Utility for managing notification channels and groups. */
public final class PdNotificationChannels {

  public static void createNotificationsChannels(Context context) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
      return;
    }

    NotificationChannelGroup group =
        new NotificationChannelGroup(REMINDERS_CHANNEL_GROUP_ID, "Task Reminders");

    NotificationChannel channel =
        new NotificationChannel(
            REMINDERS_CHANNEL_ID, "Reminders", NotificationManager.IMPORTANCE_HIGH);
    channel.setDescription("Notifications related to task reminders");
    channel.setGroup(REMINDERS_CHANNEL_GROUP_ID);

    NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
    notificationManager.createNotificationChannelGroup(group);
    notificationManager.createNotificationChannel(channel);
  }

  private PdNotificationChannels() {}
}

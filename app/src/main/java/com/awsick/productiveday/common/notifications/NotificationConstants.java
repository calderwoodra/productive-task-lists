package com.awsick.productiveday.common.notifications;

/** Utility class with list of all notification channels and groups. */
public final class NotificationConstants {

  /** Notification Channel Group ID for task reminders and related notifications. */
  public static final String REMINDERS_CHANNEL_GROUP_ID = "1000";

  /** Notification Channel ID for task reminders. */
  public static final String REMINDERS_CHANNEL_ID = "1001";

  /** Notification Group ID to enable reminders to be in a collapsible group. */
  public static final String GROUP_KEY_REMINDERS = "reminders_group";

  private NotificationConstants() {}
}

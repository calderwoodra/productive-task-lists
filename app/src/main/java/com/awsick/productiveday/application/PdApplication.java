package com.awsick.productiveday.application;

import android.app.Application;
import android.content.Context;
import androidx.hilt.work.HiltWorkerFactory;
import androidx.work.Configuration;
import com.awsick.productiveday.common.notifications.PdNotificationChannels;
import dagger.hilt.android.HiltAndroidApp;
import javax.inject.Inject;

@HiltAndroidApp
public final class PdApplication extends Application implements Configuration.Provider {

  private static PdApplication application;

  public static Context getContext() {
    return application;
  }

  @Inject HiltWorkerFactory workerFactory;

  @Override
  public void onCreate() {
    super.onCreate();
    application = this;
    PdNotificationChannels.createNotificationsChannels(this);
  }

  @Override
  public Configuration getWorkManagerConfiguration() {
    return new Configuration.Builder().setWorkerFactory(workerFactory).build();
  }
}

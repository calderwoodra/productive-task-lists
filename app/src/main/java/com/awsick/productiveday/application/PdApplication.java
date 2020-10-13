package com.awsick.productiveday.application;

import android.app.Application;
import android.content.Context;
import dagger.hilt.android.HiltAndroidApp;

@HiltAndroidApp
public final class PdApplication extends Application {

  private static Context context;

  public static Context getContext() {
    return context;
  }

  @Override
  public void onCreate() {
    super.onCreate();
    context = this;
  }
}

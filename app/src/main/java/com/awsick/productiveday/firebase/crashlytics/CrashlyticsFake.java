package com.awsick.productiveday.firebase.crashlytics;

import android.util.Log;

final class CrashlyticsFake implements Crashlytics {

  private static final String TAG = "CRASHLYTICS";

  @Override
  public void logException(String message, Throwable throwable) {
    logException(new RuntimeException(message, throwable));
  }

  @Override
  public void logException(Throwable throwable) {
    Log.e(TAG, throwable.getMessage(), throwable);
  }
}

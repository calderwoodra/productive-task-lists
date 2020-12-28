package com.awsick.productiveday.firebase.crashlytics;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

final class CrashlyticsImpl implements Crashlytics {

  private final FirebaseCrashlytics crashlytics = FirebaseCrashlytics.getInstance();

  @Override
  public void logException(String message, Throwable throwable) {
    logException(new RuntimeException(message, throwable));
  }

  @Override
  public void logException(Throwable throwable) {
    crashlytics.recordException(throwable);
  }
}

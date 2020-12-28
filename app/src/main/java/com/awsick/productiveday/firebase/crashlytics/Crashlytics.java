package com.awsick.productiveday.firebase.crashlytics;

public interface Crashlytics {

  void logException(String message, Throwable throwable);

  void logException(Throwable throwable);
}

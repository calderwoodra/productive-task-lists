package com.awsick.productiveday.firebase.crashlytics;

public interface Crashlytics {

  void logException(Throwable throwable);
}

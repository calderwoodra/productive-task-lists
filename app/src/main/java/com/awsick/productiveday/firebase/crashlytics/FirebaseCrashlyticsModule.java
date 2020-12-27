package com.awsick.productiveday.firebase.crashlytics;

import com.awsick.productiveday.BuildConfig;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.components.ApplicationComponent;
import javax.inject.Singleton;

@Module
@InstallIn(ApplicationComponent.class)
public final class FirebaseCrashlyticsModule {

  @Provides
  @Singleton
  public static Crashlytics provideCrashlytics() {
    if (BuildConfig.DEBUG) {
      return new CrashlyticsFake();
    } else {
      return new CrashlyticsImpl();
    }
  }
}

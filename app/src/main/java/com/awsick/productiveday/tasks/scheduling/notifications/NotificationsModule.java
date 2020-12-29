package com.awsick.productiveday.tasks.scheduling.notifications;

import android.content.Context;
import com.awsick.productiveday.firebase.crashlytics.Crashlytics;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.components.ApplicationComponent;
import dagger.hilt.android.qualifiers.ApplicationContext;
import java.time.Clock;
import java.util.concurrent.Executor;
import javax.inject.Singleton;

@Module
@InstallIn(ApplicationComponent.class)
public final class NotificationsModule {

  @Provides
  @Singleton
  public NotificationsRepo provideTaskDatabase(
      Clock clock,
      @ApplicationContext Context appContext,
      Executor executor,
      Crashlytics crashlytics) {
    return new NotificationsRepoImpl(clock, appContext, executor, crashlytics);
  }
}

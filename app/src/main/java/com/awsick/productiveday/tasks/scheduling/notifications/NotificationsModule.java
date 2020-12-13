package com.awsick.productiveday.tasks.scheduling.notifications;

import android.content.Context;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.components.ApplicationComponent;
import dagger.hilt.android.qualifiers.ApplicationContext;
import java.util.concurrent.Executor;
import javax.inject.Singleton;

@Module
@InstallIn(ApplicationComponent.class)
public final class NotificationsModule {

  @Provides
  @Singleton
  public NotificationsRepo provideTaskDatabase(
      @ApplicationContext Context appContext, Executor executor) {
    return new NotificationsRepoImpl(appContext, executor);
  }
}

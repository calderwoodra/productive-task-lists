package com.awsick.productiveday.tasks.repo;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.components.ApplicationComponent;
import javax.inject.Singleton;

@Module
@InstallIn(ApplicationComponent.class)
public final class TasksRepoModule {

  @Provides
  @Singleton
  public static TasksRepo provideTasksRepo() {
    return new TasksRepoImpl();
  }
}

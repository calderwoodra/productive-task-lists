package com.awsick.productiveday.directories.repo;

import com.awsick.productiveday.directories.repo.room.DirectoryDatabase;
import com.awsick.productiveday.tasks.repo.TasksRepo;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.components.ApplicationComponent;
import java.util.concurrent.Executor;
import javax.inject.Singleton;

@Module
@InstallIn(ApplicationComponent.class)
public final class DirectoriesRepoModule {

  @Provides
  @Singleton
  public static DirectoryRepo provideTasksRepo(
      DirectoryDatabase database, TasksRepo tasksRepo, Executor executor) {
    return new DirectoryRepoImpl(database, tasksRepo, executor);
  }
}

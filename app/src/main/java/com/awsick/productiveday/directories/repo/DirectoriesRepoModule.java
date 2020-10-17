package com.awsick.productiveday.directories.repo;

import android.os.Handler;
import android.os.Looper;
import com.awsick.productiveday.tasks.repo.TasksRepo;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.components.ApplicationComponent;
import java.util.concurrent.Executor;
import javax.inject.Singleton;
import org.jetbrains.annotations.NotNull;

@Module
@InstallIn(ApplicationComponent.class)
public final class DirectoriesRepoModule {

  @Provides
  @Singleton
  public static DirectoryRepo provideTasksRepo(TasksRepo tasksRepo, Executor executor) {
    return new DirectoryRepoImpl(tasksRepo, executor);
  }

  @Provides
  public static Executor executor() {
    return new Executor() {
      private final Handler mHandler = new Handler(Looper.getMainLooper());

      @Override
      public void execute(@NotNull Runnable runnable) {
        mHandler.post(runnable);
      }
    };
  }
}

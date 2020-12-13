package com.awsick.productiveday.tasks.repo;

import android.os.Handler;
import android.os.Looper;
import com.awsick.productiveday.tasks.repo.room.TaskDatabase;
import com.awsick.productiveday.tasks.scheduling.notifications.NotificationsRepo;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.components.ApplicationComponent;
import java.util.concurrent.Executor;
import javax.inject.Singleton;
import org.jetbrains.annotations.NotNull;

@Module
@InstallIn(ApplicationComponent.class)
public final class TasksRepoModule {

  @Provides
  @Singleton
  public static TasksRepo provideTasksRepo(
      TaskDatabase taskDatabase, NotificationsRepo notificationsRepo, Executor executor) {
    return new TasksRepoImpl(taskDatabase, notificationsRepo, executor);
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

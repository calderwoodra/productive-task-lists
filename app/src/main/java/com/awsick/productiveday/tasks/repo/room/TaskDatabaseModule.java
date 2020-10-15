package com.awsick.productiveday.tasks.repo.room;


import android.content.Context;
import androidx.room.Room;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.components.ApplicationComponent;
import dagger.hilt.android.qualifiers.ApplicationContext;
import javax.inject.Singleton;

@Module
@InstallIn(ApplicationComponent.class)
public final class TaskDatabaseModule {

  @Provides
  @Singleton
  public TaskDatabase provideTaskDatabase(@ApplicationContext Context appContext) {
    return Room.databaseBuilder(appContext, TaskDatabase.class, TaskDatabase.NAME).build();
  }
}

package com.awsick.productiveday.directories.repo.room;

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
public final class DirectoryDatabaseModule {

  @Provides
  @Singleton
  public DirectoryDatabase provideDirectoryDatabase(@ApplicationContext Context appContext) {
    return Room.databaseBuilder(appContext, DirectoryDatabase.class, DirectoryDatabase.NAME)
        .fallbackToDestructiveMigration()
        .build();
  }
}

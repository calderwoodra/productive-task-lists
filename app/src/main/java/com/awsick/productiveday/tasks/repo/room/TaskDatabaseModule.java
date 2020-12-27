package com.awsick.productiveday.tasks.repo.room;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.room.Room;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.components.ApplicationComponent;
import dagger.hilt.android.qualifiers.ApplicationContext;
import javax.inject.Singleton;

@Module
@InstallIn(ApplicationComponent.class)
public final class TaskDatabaseModule {

  static final Migration MIGRATION_6_7 =
      new Migration(6, 7) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
          database.execSQL(
              "ALTER TABLE taskentity"
                  + " ADD COLUMN created_millis INTEGER NOT NULL"
                  + " DEFAULT 0");
          database.execSQL(
              "ALTER TABLE taskentity"
                  + " ADD COLUMN updated_millis INTEGER NOT NULL"
                  + " DEFAULT 0");
        }
      };

  @Provides
  @Singleton
  public TaskDatabase provideTaskDatabase(@ApplicationContext Context appContext) {
    return Room.databaseBuilder(appContext, TaskDatabase.class, TaskDatabase.NAME)
        .fallbackToDestructiveMigrationFrom(1, 2, 3, 4, 5)
        .addMigrations(MIGRATION_6_7)
        .build();
  }
}

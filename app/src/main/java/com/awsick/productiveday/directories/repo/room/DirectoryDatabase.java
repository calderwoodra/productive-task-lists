package com.awsick.productiveday.directories.repo.room;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(
    entities = {DirectoryEntity.class},
    version = 1)
public abstract class DirectoryDatabase extends RoomDatabase {

  public static final String NAME = "DIRECTORY_DATABASE";

  public abstract DirectoryDao directoryDao();
}

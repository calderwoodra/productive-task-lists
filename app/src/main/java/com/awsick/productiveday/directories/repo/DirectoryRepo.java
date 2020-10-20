package com.awsick.productiveday.directories.repo;

import androidx.lifecycle.LiveData;
import com.awsick.productiveday.directories.models.Directory;
import com.awsick.productiveday.directories.repo.room.DirectoryDatabase;
import com.awsick.productiveday.network.RequestStatus;

/** Repo for fetching directory data. */
public interface DirectoryRepo {

  int ROOT_DIRECTORY_ID = DirectoryDatabase.ROOT_DIRECTORY_ID;

  /** Returns a directory with the Unique ID (@code uid}. */
  LiveData<RequestStatus<Directory>> getDirectory(int uid);

  /** Create a new directory with nothing in it. */
  void createDirectory(String name, int currentDirectory);
}

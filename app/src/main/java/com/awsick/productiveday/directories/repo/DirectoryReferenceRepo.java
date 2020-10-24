package com.awsick.productiveday.directories.repo;

import androidx.lifecycle.LiveData;
import com.awsick.productiveday.directories.models.DirectoryReference;
import com.awsick.productiveday.directories.repo.room.DirectoryDatabase;
import com.awsick.productiveday.network.RequestStatus;
import com.google.common.collect.ImmutableList;

/** Repo for fetching directory data. */
public interface DirectoryReferenceRepo {

  int ROOT_DIRECTORY_ID = DirectoryDatabase.ROOT_DIRECTORY_ID;

  /** Returns a {@link DirectoryReference} with the Unique ID (@code uid}. */
  LiveData<RequestStatus<DirectoryReference>> getDirectory(int uid);

  /** Create a new directory with nothing in it. */
  void createDirectory(String name, int currentDirectory);

  /** Update's {@code directory} name to {@code updatedName}. Home directory cannot be renamed. */
  void updateDirectory(DirectoryReference directory, String updatedName);

  /** Return list of directories whose direct parent is {@code uid}. */
  LiveData<RequestStatus<ImmutableList<DirectoryReference>>> getChildDirectories(int uid);
}

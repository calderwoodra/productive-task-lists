package com.awsick.productiveday.directories.repo;

import androidx.lifecycle.LiveData;
import com.awsick.productiveday.directories.models.Directory;
import com.awsick.productiveday.network.RequestStatus;

/** Repo for fetching directory data. */
public interface DirectoryRepo {

  /** Returns a directory with the Unique ID (@code uid}. */
  LiveData<RequestStatus<Directory>> getDirectory(int uid);
}

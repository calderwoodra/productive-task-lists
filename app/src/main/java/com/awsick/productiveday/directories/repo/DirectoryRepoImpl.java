package com.awsick.productiveday.directories.repo;

import androidx.lifecycle.LiveData;
import com.awsick.productiveday.directories.models.Directory;
import com.awsick.productiveday.directories.models.DirectoryReference;
import com.awsick.productiveday.network.RequestStatus;
import com.awsick.productiveday.network.util.RequestStatusLiveData;
import com.awsick.productiveday.tasks.repo.TasksRepo;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import java.util.HashMap;
import java.util.concurrent.Executor;
import javax.inject.Inject;
import javax.inject.Singleton;

/** Repo for fetching directory data. */
@Singleton
final class DirectoryRepoImpl implements DirectoryRepo {

  private final Executor executor;
  private final TasksRepo tasksRepo;
  private final HashMap<Integer, RequestStatusLiveData<Directory>> directoryCache = new HashMap<>();

  @Inject
  DirectoryRepoImpl(TasksRepo tasksRepo, Executor executor) {
    this.tasksRepo = tasksRepo;
    this.executor = executor;
    create(-1);
  }

  @Override
  public LiveData<RequestStatus<Directory>> getDirectory(int uid) {
    if (!directoryCache.containsKey(uid)) {
      return directoryCache.put(uid, create(uid));
    }
    return directoryCache.get(uid);
  }

  private RequestStatusLiveData<Directory> create(int uid) {
    RequestStatusLiveData<Directory> directory = new RequestStatusLiveData<>();
    refreshDirectory(directory, uid);
    return directory;
  }

  private void refreshDirectory(RequestStatusLiveData<Directory> directory, int uid) {
    directory.setValue(
        RequestStatus.success(
            Directory.builder()
                .setReference(
                    DirectoryReference.builder()
                        .setName("Home")
                        .setUid(uid)
                        .setParent(
                            uid == -1
                                ? Optional.absent()
                                : Optional.of(getDirectory(-1).getValue().getResult().reference()))
                        .build())
                .setDirectories(ImmutableList.of())
                .setTasks(ImmutableList.of())
                .build()));
  }
}

package com.awsick.productiveday.directories.repo;

import static com.awsick.productiveday.common.utils.ImmutableUtils.toImmutableList;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.awsick.productiveday.directories.models.DirectoryReference;
import com.awsick.productiveday.directories.repo.room.DirectoryDatabase;
import com.awsick.productiveday.directories.repo.room.DirectoryEntity;
import com.awsick.productiveday.network.RequestStatus;
import com.awsick.productiveday.network.util.RequestStatusLiveData;
import com.awsick.productiveday.network.util.SimpleFutureCallback;
import com.awsick.productiveday.network.util.SimpleFutures;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.Futures;
import java.util.HashMap;
import java.util.concurrent.Executor;
import javax.inject.Inject;
import javax.inject.Singleton;

/** Repo for fetching directory reference data. */
@Singleton
final class DirectoryReferenceRepoImpl implements DirectoryReferenceRepo {

  private final HashMap<Integer, RequestStatusLiveData<DirectoryReference>> directoryCache =
      new HashMap<>();
  private final HashMap<Integer, RequestStatusLiveData<ImmutableList<DirectoryReference>>>
      childDirectoriesCache = new HashMap<>();
  private final MutableLiveData<Boolean> rootDirectoryReady = new MutableLiveData<>(false);
  private final DirectoryDatabase directoryDatabase;
  private final Executor executor;

  @Inject
  DirectoryReferenceRepoImpl(DirectoryDatabase directoryDatabase, Executor executor) {
    this.directoryDatabase = directoryDatabase;
    this.executor = executor;
    init();
  }

  // Check that the root directory exists and create it if it doesn't.
  private void init() {
    SimpleFutures.addCallback(
        directoryDatabase.directoryDao().getDirectory(ROOT_DIRECTORY_ID),
        readResult -> {
          if (readResult != null) {
            rootDirectoryReady.setValue(true);
            return;
          }
          Futures.addCallback(
              directoryDatabase
                  .directoryDao()
                  .insert(
                      DirectoryEntity.from(
                          DirectoryReference.builder()
                              .setParent(Optional.absent())
                              .setName("Home")
                              .setUid(ROOT_DIRECTORY_ID)
                              .build())),
              (SimpleFutureCallback<Long>) insertResult -> rootDirectoryReady.setValue(true),
              executor);
        },
        executor);
  }

  @Override
  public LiveData<RequestStatus<DirectoryReference>> getDirectory(int uid) {
    if (!directoryCache.containsKey(uid)) {
      directoryCache.put(uid, fetchDirectory(new RequestStatusLiveData<>(), uid));
    }
    return directoryCache.get(uid);
  }

  private RequestStatusLiveData<DirectoryReference> fetchDirectory(
      RequestStatusLiveData<DirectoryReference> directoryReference, int uid) {
    directoryReference.setValue(RequestStatus.pending());
    SimpleFutures.addCallback(
        directoryDatabase.directoryDao().getDirectory(uid),
        directory ->
            directoryReference.setValue(RequestStatus.success(directory.toDirectoryReference())),
        executor);
    return directoryReference;
  }

  private void refreshDirectory(int uid) {
    if (!directoryCache.containsKey(uid)) {
      return;
    }
    RequestStatusLiveData<DirectoryReference> liveData = directoryCache.get(uid);
    fetchDirectory(liveData, uid);
  }

  @Override
  public void createDirectory(String name, int currentDirectory) {
    DirectoryEntity entity =
        DirectoryEntity.from(
            DirectoryReference.builder()
                .setName(name)
                .setParent(Optional.of(currentDirectory))
                .build());

    SimpleFutures.addCallback(
        directoryDatabase.directoryDao().insert(entity),
        uid -> refreshDirectory(currentDirectory),
        executor);
  }

  @Override
  public void updateDirectory(DirectoryReference directory, String updatedName) {
    DirectoryEntity entity =
        DirectoryEntity.from(
            DirectoryReference.builder()
                .setUid(directory.uid())
                .setParent(directory.parent())
                .setName(updatedName)
                .build());

    SimpleFutures.addCallback(
        directoryDatabase.directoryDao().update(entity),
        voidd -> refreshDirectory(directory.parent().get()),
        executor);
  }

  @Override
  public LiveData<RequestStatus<ImmutableList<DirectoryReference>>> getChildDirectories(int uid) {
    if (!childDirectoriesCache.containsKey(uid)) {
      childDirectoriesCache.put(uid, fetchChildDirectories(uid));
    }
    return childDirectoriesCache.get(uid);
  }

  private RequestStatusLiveData<ImmutableList<DirectoryReference>> fetchChildDirectories(int uid) {
    RequestStatusLiveData<ImmutableList<DirectoryReference>> children =
        new RequestStatusLiveData<>();
    children.setValue(RequestStatus.pending());
    SimpleFutures.addCallback(
        directoryDatabase.directoryDao().getSiblingDirectories(uid),
        siblings ->
            children.setValue(
                RequestStatus.success(
                    siblings.stream()
                        .map(DirectoryEntity::toDirectoryReference)
                        .collect(toImmutableList()))),
        executor);
    return children;
  }
}

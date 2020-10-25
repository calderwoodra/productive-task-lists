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
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.Futures;
import java.util.HashMap;
import java.util.Objects;
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
              directoryDatabase.directoryDao().insert(DirectoryEntity.from(ROOT)),
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

  @Override
  public void createDirectory(String name, int currentDirectory) {
    DirectoryEntity entity = new DirectoryEntity();
    entity.name = name;
    entity.parentUid = currentDirectory;

    SimpleFutures.addCallback(
        directoryDatabase.directoryDao().insert(entity),
        uid -> refreshChildDirectories(currentDirectory),
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
        voidd -> {
          refreshDirectory(directory.uid());
          refreshChildDirectories(directory.parent().get());
        },
        executor);
  }

  private void refreshDirectory(int uid) {
    fetchDirectory(directoryCache.get(uid), uid);
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

  @Override
  public LiveData<RequestStatus<ImmutableList<DirectoryReference>>> getChildDirectories(int uid) {
    if (!childDirectoriesCache.containsKey(uid)) {
      childDirectoriesCache.put(uid, fetchChildDirectories(new RequestStatusLiveData<>(), uid));
    }
    return childDirectoriesCache.get(uid);
  }

  private void refreshChildDirectories(int uid) {
    fetchChildDirectories(Objects.requireNonNull(childDirectoriesCache.get(uid)), uid);
  }

  private RequestStatusLiveData<ImmutableList<DirectoryReference>> fetchChildDirectories(
      RequestStatusLiveData<ImmutableList<DirectoryReference>> children, int uid) {
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

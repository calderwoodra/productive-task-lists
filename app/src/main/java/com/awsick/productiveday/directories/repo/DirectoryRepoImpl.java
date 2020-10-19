package com.awsick.productiveday.directories.repo;

import static com.awsick.productiveday.common.utils.ImmutableUtils.toImmutableList;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import com.awsick.productiveday.directories.models.Directory;
import com.awsick.productiveday.directories.models.DirectoryReference;
import com.awsick.productiveday.directories.repo.room.DirectoryDatabase;
import com.awsick.productiveday.directories.repo.room.DirectoryEntity;
import com.awsick.productiveday.network.RequestStatus;
import com.awsick.productiveday.network.RequestStatus.Status;
import com.awsick.productiveday.network.util.RequestStatusLiveData;
import com.awsick.productiveday.network.util.RequestStatusUtils;
import com.awsick.productiveday.network.util.SimpleFutureCallback;
import com.awsick.productiveday.network.util.SimpleFutures;
import com.awsick.productiveday.tasks.models.Task;
import com.awsick.productiveday.tasks.repo.TasksRepo;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.Futures;
import java.util.HashMap;
import java.util.concurrent.Executor;
import javax.inject.Inject;
import javax.inject.Singleton;

/** Repo for fetching directory data. */
@Singleton
final class DirectoryRepoImpl implements DirectoryRepo {

  private final HashMap<Integer, LiveData<RequestStatus<Directory>>> directoryCache =
      new HashMap<>();
  private final MutableLiveData<Boolean> rootDirectoryReady = new MutableLiveData<>(false);
  private final DirectoryDatabase directoryDatabase;
  private final TasksRepo tasksRepo;
  private final Executor executor;

  @Inject
  DirectoryRepoImpl(DirectoryDatabase directoryDatabase, TasksRepo tasksRepo, Executor executor) {
    this.directoryDatabase = directoryDatabase;
    this.tasksRepo = tasksRepo;
    this.executor = executor;
    init();
  }

  // Check that the root directory exists and create it if it doesn't.
  private void init() {
    SimpleFutures.addCallback(
        directoryDatabase.directoryDao().getDirectory(-1),
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
                              .setUid(-1)
                              .build())),
              (SimpleFutureCallback<Long>) insertResult -> rootDirectoryReady.setValue(true),
              executor);
        },
        executor);
  }

  @Override
  public LiveData<RequestStatus<Directory>> getDirectory(int uid) {
    if (uid == ROOT_DIRECTORY_ID) {
      return Transformations.switchMap(
          rootDirectoryReady,
          ready -> {
            if (!ready) {
              return new RequestStatusLiveData<>();
            }
            return getDirectoryInternal(-1);
          });
    }
    return getDirectoryInternal(uid);
  }

  public LiveData<RequestStatus<Directory>> getDirectoryInternal(int uid) {
    if (!directoryCache.containsKey(uid)) {
      directoryCache.put(uid, fetchDirectory(uid));
    }
    return directoryCache.get(uid);
  }

  private LiveData<RequestStatus<Directory>> fetchDirectory(int uid) {
    MutableLiveData<DirectoryReference> directoryReference = new MutableLiveData<>();
    SimpleFutures.addCallback(
        directoryDatabase.directoryDao().getDirectory(uid),
        directory -> directoryReference.setValue(directory.toDirectoryReference()),
        executor);

    return new DirectoryLiveData(tasksRepo, directoryDatabase, directoryReference, executor);
  }

  private static final class DirectoryLiveData extends MediatorLiveData<RequestStatus<Directory>> {

    private final LiveData<DirectoryReference> reference;

    private RequestStatus<ImmutableList<Task>> tasks = RequestStatus.initial();
    private RequestStatus<ImmutableList<DirectoryReference>> directories = RequestStatus.initial();

    public DirectoryLiveData(
        TasksRepo tasksRepo,
        DirectoryDatabase database,
        LiveData<DirectoryReference> reference,
        Executor executor) {
      this.reference = reference;
      setValue(RequestStatus.pending());

      addSource(
          Transformations.switchMap(
              reference, result -> tasksRepo.getIncompleteTasks(result.uid())),
          tasks -> {
            this.tasks = tasks == null ? RequestStatus.pending() : tasks;
            onChanged();
          });

      LiveData<RequestStatus<ImmutableList<DirectoryReference>>> foo =
          Transformations.switchMap(
              reference,
              referenceResult -> {
                RequestStatusLiveData<ImmutableList<DirectoryReference>> directoriesLiveData =
                    new RequestStatusLiveData<>();
                SimpleFutures.addCallback(
                    database.directoryDao().getSiblingDirectories(referenceResult.uid()),
                    result ->
                        directoriesLiveData.setValue(
                            RequestStatus.success(
                                result.stream()
                                    .map(DirectoryEntity::toDirectoryReference)
                                    .collect(toImmutableList()))),
                    executor);
                return directoriesLiveData;
              });

      addSource(
          foo,
          directories -> {
            this.directories = directories;
            onChanged();
          });
    }

    private void onChanged() {
      if (RequestStatusUtils.areAllSuccess(tasks, directories)) {
        setValue(
            RequestStatus.success(
                Directory.builder()
                    .setTasks(tasks.getResult())
                    .setDirectories(directories.getResult())
                    .setReference(reference.getValue())
                    .build()));
        return;
      }

      if (tasks.status == Status.FAILED) {
        setValue(RequestStatus.error(tasks.getError()));

      } else if (directories.status == Status.FAILED) {
        setValue(RequestStatus.error(directories.getError()));

      } else {
        setValue(RequestStatus.pending());
      }
    }
  }
}

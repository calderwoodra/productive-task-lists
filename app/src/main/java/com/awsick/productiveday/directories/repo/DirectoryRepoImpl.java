package com.awsick.productiveday.directories.repo;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import com.awsick.productiveday.directories.models.Directory;
import com.awsick.productiveday.directories.models.DirectoryReference;
import com.awsick.productiveday.network.RequestStatus;
import com.awsick.productiveday.network.RequestStatus.Status;
import com.awsick.productiveday.network.util.RequestStatusUtils;
import com.awsick.productiveday.tasks.models.Task;
import com.awsick.productiveday.tasks.repo.TasksRepo;
import com.google.common.collect.ImmutableList;
import java.util.HashMap;
import javax.inject.Inject;
import javax.inject.Singleton;

/** Repo for fetching directory data (references and tasks contained in directories). */
@Singleton
final class DirectoryRepoImpl implements DirectoryRepo {

  private final HashMap<Integer, DirectoryLiveData> directoryCache = new HashMap<>();
  private final DirectoryReferenceRepo directoryReferenceRepo;
  private final TasksRepo tasksRepo;

  @Inject
  DirectoryRepoImpl(DirectoryReferenceRepo directoryReferenceRepo, TasksRepo tasksRepo) {
    this.directoryReferenceRepo = directoryReferenceRepo;
    this.tasksRepo = tasksRepo;
  }

  @Override
  public LiveData<RequestStatus<Directory>> getDirectory(int uid) {
    if (!directoryCache.containsKey(uid)) {
      // Create the livedata for the directory if it doesn't exist yet
      directoryCache.put(uid, fetchDirectory(uid));
    }
    return directoryCache.get(uid);
  }

  private DirectoryLiveData fetchDirectory(int uid) {
    return new DirectoryLiveData(
        directoryReferenceRepo, tasksRepo, directoryReferenceRepo.getDirectory(uid));
  }

  private static final class DirectoryLiveData extends MediatorLiveData<RequestStatus<Directory>> {

    private final LiveData<RequestStatus<DirectoryReference>> reference;

    private RequestStatus<ImmutableList<Task>> tasks = RequestStatus.initial();
    private RequestStatus<ImmutableList<DirectoryReference>> directories = RequestStatus.initial();

    public DirectoryLiveData(
        DirectoryReferenceRepo directoryReferenceRepo,
        TasksRepo tasksRepo,
        LiveData<RequestStatus<DirectoryReference>> reference) {
      this.reference = reference;
      setValue(RequestStatus.pending());

      // Fetch list of all tasks contained in reference
      addSource(
          Transformations.switchMap(
              reference,
              input -> {
                if (input.status != Status.SUCCESS) {
                  return new MutableLiveData<>(RequestStatus.pending());
                }
                return tasksRepo.getIncompleteTasks(input.getResult().uid());
              }),
          tasks -> {
            this.tasks = tasks == null ? RequestStatus.pending() : tasks;
            onChanged();
          });

      // Fetch list of all directories contained in reference
      addSource(
          Transformations.switchMap(
              reference,
              input -> {
                if (input.status != Status.SUCCESS) {
                  return new MutableLiveData<>(RequestStatus.pending());
                }
                return directoryReferenceRepo.getChildDirectories(input.getResult().uid());
              }),
          directories -> {
            this.directories = directories;
            onChanged();
          });
    }

    private void onChanged() {
      if (RequestStatusUtils.areAllSuccess(reference.getValue(), tasks, directories)) {
        setValue(
            RequestStatus.success(
                Directory.builder()
                    .setTasks(tasks.getResult())
                    .setDirectories(directories.getResult())
                    .setReference(reference.getValue().getResult())
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

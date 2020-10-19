package com.awsick.productiveday.directories.ui;

import androidx.hilt.lifecycle.ViewModelInject;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.awsick.productiveday.directories.models.Directory;
import com.awsick.productiveday.directories.repo.DirectoryRepo;
import com.awsick.productiveday.network.RequestStatus;
import com.awsick.productiveday.tasks.models.Task;
import com.awsick.productiveday.tasks.repo.TasksRepo;

public final class DirectoryBrowseViewModel extends ViewModel {

  private final MutableLiveData<Integer> currentUid =
      new MutableLiveData<>(DirectoryRepo.ROOT_DIRECTORY_ID);

  private final CurrentDirectoryLiveData currentDirectory;
  private final TasksRepo tasksRepo;

  @ViewModelInject
  DirectoryBrowseViewModel(DirectoryRepo directoryRepo, TasksRepo tasksRepo) {
    this.tasksRepo = tasksRepo;
    currentDirectory = new CurrentDirectoryLiveData(directoryRepo, currentUid);
  }

  public LiveData<RequestStatus<Directory>> getCurrentDirectory() {
    return currentDirectory;
  }

  public void setCurrentDirectory(int uid) {
    currentUid.setValue(uid);
  }

  public void markTaskCompleted(Task task) {
    tasksRepo.markTaskCompleted(task);
  }

  private static final class CurrentDirectoryLiveData
      extends MediatorLiveData<RequestStatus<Directory>> {

    private LiveData<RequestStatus<Directory>> currentDirectory = null;

    public CurrentDirectoryLiveData(DirectoryRepo directoryRepo, LiveData<Integer> uidLiveData) {
      addSource(
          uidLiveData,
          uid -> {
            if (currentDirectory != null) {
              removeSource(currentDirectory);
            }
            currentDirectory = directoryRepo.getDirectory(uid);
            addSource(currentDirectory, this::setValue);
          });
    }
  }
}

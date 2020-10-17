package com.awsick.productiveday.directories.ui;

import androidx.hilt.lifecycle.ViewModelInject;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.awsick.productiveday.directories.models.Directory;
import com.awsick.productiveday.directories.repo.DirectoryRepo;
import com.awsick.productiveday.network.RequestStatus;

public final class DirectoryBrowseViewModel extends ViewModel {

  private final MutableLiveData<Integer> currentUid = new MutableLiveData<>(-1);
  private final CurrentDirectoryLiveData currentDirectory;

  @ViewModelInject
  DirectoryBrowseViewModel(DirectoryRepo directoryRepo) {
    currentDirectory = new CurrentDirectoryLiveData(directoryRepo, currentUid);
  }

  public LiveData<RequestStatus<Directory>> getCurrentDirectory() {
    return currentDirectory;
  }

  public void setCurrentDirectory(int uid) {
    currentUid.setValue(uid);
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

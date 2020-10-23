package com.awsick.productiveday.tasks.create;

import androidx.hilt.Assisted;
import androidx.hilt.lifecycle.ViewModelInject;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;
import com.awsick.productiveday.BuildConfig;
import com.awsick.productiveday.common.utils.DateUtils;
import com.awsick.productiveday.common.viewmodelutils.SingleLiveEvent;
import com.awsick.productiveday.directories.models.Directory;
import com.awsick.productiveday.directories.repo.DirectoryRepo;
import com.awsick.productiveday.network.RequestStatus;
import com.awsick.productiveday.network.util.RequestStatusUtils;
import com.awsick.productiveday.tasks.models.Task;
import com.awsick.productiveday.tasks.models.Task.Type;
import com.awsick.productiveday.tasks.models.TaskRepeatability;
import com.awsick.productiveday.tasks.repo.TasksRepo;

public final class TasksCreateViewModel extends ViewModel {

  public enum SaveEvents {
    SUCCESSFULLY_SAVED,
    FAILED_TO_SAVE,
    TITLE_MISSING,
  }

  // Repos
  private final TasksRepo tasksRepo;
  private final DirectoryRepo directoryRepo;

  // Basics
  private final int taskId;
  private final MutableLiveData<String> title =
      new MutableLiveData<>(BuildConfig.DEBUG ? "Test Title" : "");
  private final MutableLiveData<String> notes =
      new MutableLiveData<>(BuildConfig.DEBUG ? "Test Notes" : "");

  // Scheduling
  private final MutableLiveData<Task.Type> taskType = new MutableLiveData<>(Type.UNSCHEDULED);
  private final MutableLiveData<Long> timeMillis = new MutableLiveData<>(midnightTonight());
  private final MutableLiveData<TaskRepeatability> repeatable = new MutableLiveData<>(null);

  // Directories
  private final MutableLiveData<Integer> directoryId = new MutableLiveData<>(-1);
  private final DirectoryNameLiveData directoryName;

  // Single Consumables
  private final SingleLiveEvent<SaveEvents> saveEvents = new SingleLiveEvent<>();

  @ViewModelInject
  TasksCreateViewModel(
      TasksRepo tasksRepo, DirectoryRepo directoryRepo, @Assisted SavedStateHandle savedState) {
    this.tasksRepo = tasksRepo;
    this.directoryRepo = directoryRepo;
    directoryName = new DirectoryNameLiveData(directoryRepo, directoryId);

    Integer taskId = savedState.get(TasksCreateFragment.TASK_ID_KEY);
    if (taskId == null || taskId == -1) {
      this.taskId = -1;
      return;
    }

    Task task =
        tasksRepo.getIncompleteTasks().getValue().getResult().stream()
            .filter(t -> t.uid() == taskId)
            .findFirst()
            .orElse(null);
    if (task == null) {
      this.taskId = -1;
      return;
    }

    this.taskId = taskId;
    title.setValue(task.title());
    notes.setValue(task.notes());
    taskType.setValue(task.type());
    directoryId.setValue(task.directoryId());
  }

  public LiveData<String> getTitle() {
    return title;
  }

  public LiveData<String> getNotes() {
    return notes;
  }

  public LiveData<String> getDate() {
    return Transformations.map(timeMillis, DateUtils::humanReadableDate);
  }

  public LiveData<String> getTime() {
    return Transformations.map(timeMillis, DateUtils::humanReadableTime);
  }

  public LiveData<String> getRepeatable() {
    return Transformations.map(
        repeatable,
        repeat -> {
          if (repeat == null) {
            return "Does not repeat";
          }
          return repeat.toString();
        });
  }

  public void setDirectoryId(int id) {
    directoryId.setValue(id);
  }

  public LiveData<RequestStatus<String>> getDirectoryName() {
    return directoryName;
  }

  public LiveData<SaveEvents> getSaveEvents() {
    return saveEvents;
  }

  public LiveData<Boolean> schedulingVisible() {
    return Transformations.map(taskType, task -> task != Type.UNSCHEDULED);
  }

  public LiveData<Boolean> repeatVisible() {
    return Transformations.map(taskType, task -> task == Type.REMINDER);
  }

  public void setTaskType(Type type) {
    taskType.setValue(type);
  }

  public LiveData<Type> getTaskType() {
    return taskType;
  }

  private static long midnightTonight() {
    // TODO(allen): Calculate 11:59 PM tonight
    return 0;
  }

  public void saveTask(String title, String notes) {
    this.title.setValue(title);
    this.notes.setValue(notes);
    Task.Builder taskBuilder =
        Task.builder()
            .setTitle(title)
            .setNotes(notes)
            // TODO(allen): implement these
            // .setDeadlineMillis(timeMillis.getValue())
            // .setRepeatability(repeatable.getValue())
            // .setDirectoryId(-1)
            .setType(taskType.getValue());

    if (taskId == -1) {
      tasksRepo.createTask(taskBuilder.build());
    } else {
      tasksRepo.updateTask(taskBuilder.setUid(taskId).build());
    }
    saveEvents.setValue(SaveEvents.SUCCESSFULLY_SAVED);
  }

  private static final class DirectoryNameLiveData extends MediatorLiveData<RequestStatus<String>> {

    private LiveData<RequestStatus<Directory>> currentDirectory = null;

    public DirectoryNameLiveData(DirectoryRepo directoryRepo, LiveData<Integer> uidLiveData) {
      setValue(RequestStatus.initial());
      addSource(
          uidLiveData,
          uid -> {
            if (currentDirectory != null) {
              removeSource(currentDirectory);
            }
            currentDirectory = directoryRepo.getDirectory(uid);
            addSource(
                currentDirectory,
                directory -> {
                  setValue(RequestStatusUtils.identity(directory, d -> d.reference().name()));
                });
          });
    }
  }
}

package com.awsick.productiveday.tasks.create;

import androidx.hilt.lifecycle.ViewModelInject;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;
import com.awsick.productiveday.BuildConfig;
import com.awsick.productiveday.common.utils.DateUtils;
import com.awsick.productiveday.common.viewmodelutils.SingleLiveEvent;
import com.awsick.productiveday.tasks.models.Task;
import com.awsick.productiveday.tasks.models.TaskRepeatability;
import com.awsick.productiveday.tasks.repo.TasksRepo;

public final class TasksCreateViewModel extends ViewModel {

  public enum SaveEvents {
    SUCCESSFULLY_SAVED,
    FAILED_TO_SAVE,
    TITLE_MISSING,
  }

  private final TasksRepo tasksRepo;
  private final MutableLiveData<String> title =
      new MutableLiveData<>(BuildConfig.DEBUG ? "Test Title" : "");
  private final MutableLiveData<String> notes =
      new MutableLiveData<>(BuildConfig.DEBUG ? "Test Notes" : "");
  private final MutableLiveData<Long> timeMillis = new MutableLiveData<>(midnightTonight());
  private final MutableLiveData<TaskRepeatability> repeatable = new MutableLiveData<>(null);
  private final MutableLiveData<String> directoryName = new MutableLiveData<>("Uncategorized");
  private final SingleLiveEvent<SaveEvents> saveEvents = new SingleLiveEvent<>();

  @ViewModelInject
  TasksCreateViewModel(TasksRepo tasksRepo) {
    this.tasksRepo = tasksRepo;
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

  public LiveData<String> getDirectoryName() {
    return directoryName;
  }

  public LiveData<SaveEvents> getSaveEvents() {
    return saveEvents;
  }

  private static long midnightTonight() {
    // TODO(allen): Calculate 11:59 PM tonight
    return 0;
  }

  public void saveTask(String title, String notes) {
    this.title.setValue(title);
    this.notes.setValue(notes);
    tasksRepo.createTask(
        Task.builder()
            .setTitle(title)
            .setNotes(notes)
            // TODO(allen): implement these
            // .setDeadlineMillis(timeMillis.getValue())
            // .setRepeatability(repeatable.getValue())
            // .setDirectoryId(-1)
            .build());
    saveEvents.setValue(SaveEvents.SUCCESSFULLY_SAVED);
  }
}

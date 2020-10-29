package com.awsick.productiveday.tasks.create;

import static com.awsick.productiveday.tasks.create.TaskCreateActivity.TASK_ID_KEY;

import androidx.hilt.Assisted;
import androidx.hilt.lifecycle.ViewModelInject;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;
import com.awsick.productiveday.BuildConfig;
import com.awsick.productiveday.common.utils.Assert;
import com.awsick.productiveday.common.utils.DateUtils;
import com.awsick.productiveday.common.viewmodelutils.SingleLiveEvent;
import com.awsick.productiveday.directories.models.Directory;
import com.awsick.productiveday.directories.repo.DirectoryReferenceRepo;
import com.awsick.productiveday.directories.repo.DirectoryRepo;
import com.awsick.productiveday.network.RequestStatus;
import com.awsick.productiveday.network.util.RequestStatusUtils;
import com.awsick.productiveday.tasks.models.Task;
import com.awsick.productiveday.tasks.models.Task.Type;
import com.awsick.productiveday.tasks.models.TaskRepeatability;
import com.awsick.productiveday.tasks.repo.TasksRepo;
import com.google.common.base.Optional;
import java.util.Calendar;
import java.util.TimeZone;

public final class TasksCreateViewModel extends ViewModel {

  public enum SaveEvents {
    SUCCESSFULLY_SAVED,
    FAILED_TO_SAVE,
    TITLE_MISSING,
  }

  // Repos
  private final TasksRepo tasksRepo;

  // Basics
  private final Task existingTask;
  private final int taskId;
  private final MutableLiveData<String> title =
      new MutableLiveData<>(BuildConfig.DEBUG ? "Test Title" : "");
  private final MutableLiveData<String> notes =
      new MutableLiveData<>(BuildConfig.DEBUG ? "Test Notes" : "");

  // Scheduling
  private final MutableLiveData<Task.Type> taskType = new MutableLiveData<>(Type.UNSCHEDULED);
  private final MutableLiveData<Long> timeMillis = new MutableLiveData<>(midnightTonight());
  private final MutableLiveData<Optional<TaskRepeatability>> repeatable =
      new MutableLiveData<>(Optional.absent());

  // Directories
  private final MutableLiveData<Integer> directoryId =
      new MutableLiveData<>(DirectoryReferenceRepo.ROOT_DIRECTORY_ID);
  private final LiveData<RequestStatus<Directory>> directory;
  private final LiveData<RequestStatus<String>> directoryName;

  // Single Consumables
  private final SingleLiveEvent<SaveEvents> saveEvents = new SingleLiveEvent<>();

  @ViewModelInject
  TasksCreateViewModel(
      TasksRepo tasksRepo, DirectoryRepo directoryRepo, @Assisted SavedStateHandle savedState) {
    this.tasksRepo = tasksRepo;
    directory = Transformations.switchMap(directoryId, directoryRepo::getDirectory);
    directoryName =
        Transformations.map(
            directory, status -> RequestStatusUtils.identity(status, d -> d.reference().name()));

    Integer taskId = savedState.get(TASK_ID_KEY);
    if (taskId == null || taskId == -1) {
      existingTask = null;
      this.taskId = -1;
      return;
    }

    Task existingTask =
        tasksRepo.getIncompleteTasks().getValue().getResult().stream()
            .filter(t -> t.uid() == taskId)
            .findFirst()
            .orElse(null);
    if (existingTask == null) {
      this.existingTask = null;
      this.taskId = -1;
      return;
    }

    this.existingTask = existingTask;
    this.taskId = taskId;
    title.setValue(existingTask.title());
    notes.setValue(existingTask.notes());
    taskType.setValue(existingTask.type());
    timeMillis.setValue(existingTask.deadlineMillis());
    directoryId.setValue(existingTask.directoryId());
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
          if (!repeat.isPresent()) {
            return "Does not repeat";
          }
          return repeat.get().toString();
        });
  }

  public LiveData<Boolean> showClearRepeatable() {
    return Transformations.map(repeatable, Optional::isPresent);
  }

  public int getCurrentDirectory() {
    return directoryId.getValue();
  }

  public void setDirectoryId(int id) {
    directoryId.setValue(id);
  }

  public LiveData<RequestStatus<Directory>> getDirectory() {
    return directory;
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
    Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
    calendar.set(Calendar.HOUR, 11);
    calendar.set(Calendar.MINUTE, 59);
    return calendar.toInstant().getEpochSecond() * 1000;
  }

  public Calendar getCalendar() {
    Calendar calendar = Calendar.getInstance();
    calendar.setTimeInMillis(timeMillis.getValue());
    return calendar;
  }

  public void setDate(int year, int month, int dayOfMonth) {
    Calendar calendar = getCalendar();
    calendar.set(Calendar.YEAR, year);
    calendar.set(Calendar.MONTH, month);
    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
    timeMillis.setValue(calendar.getTimeInMillis());
  }

  public void setTime(int hourOfDay, int minute) {
    Calendar calendar = getCalendar();
    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
    calendar.set(Calendar.MINUTE, minute);
    timeMillis.setValue(calendar.getTimeInMillis());
  }

  public void saveTask(String title, String notes) {
    this.title.setValue(title);
    this.notes.setValue(notes);
    Task.Builder taskBuilder =
        Task.builder()
            .setTitle(title)
            .setNotes(notes)
            .setDeadlineMillis(taskType.getValue() == Type.UNSCHEDULED ? -1 : timeMillis.getValue())
            .setRepeatability(repeatable.getValue().orNull())
            .setDirectoryId(directoryId.getValue())
            .setType(taskType.getValue());

    if (taskId == -1) {
      tasksRepo.createTask(taskBuilder.build());
    } else {
      tasksRepo.updateTask(existingTask, taskBuilder.setUid(taskId).build());
    }
    saveEvents.setValue(SaveEvents.SUCCESSFULLY_SAVED);
  }

  public void setRepeatability(TaskRepeatability taskRepeatability) {
    repeatable.setValue(Optional.of(taskRepeatability));
  }

  public void clearRepeat() {
    Assert.checkArgument(repeatable.getValue().isPresent());
    repeatable.setValue(Optional.absent());
  }
}

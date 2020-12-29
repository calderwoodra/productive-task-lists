package com.awsick.productiveday.tasks.create;

import static com.awsick.productiveday.tasks.create.TaskCreateActivity.DIRECTORY_ID_KEY;
import static com.awsick.productiveday.tasks.create.TaskCreateActivity.TASK_ID_KEY;

import android.content.Context;
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
import com.awsick.productiveday.common.viewmodelutils.MergerLiveData;
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
import dagger.hilt.android.qualifiers.ApplicationContext;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.concurrent.TimeUnit;

public final class TasksCreateViewModel extends ViewModel {

  public enum SaveEvents {
    SUCCESSFULLY_SAVED,
    FAILED_TO_SAVE,
    TITLE_MISSING,
  }

  private final Context context;
  private final Clock clock;

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
  private final MutableLiveData<Long> timeMillis;
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
      @ApplicationContext Context context,
      Clock clock,
      TasksRepo tasksRepo,
      DirectoryRepo directoryRepo,
      @Assisted SavedStateHandle savedState) {
    this.context = context;
    this.clock = clock;
    this.tasksRepo = tasksRepo;
    timeMillis = new MutableLiveData<>(midnightTonight(clock));

    Integer passedDirectory = savedState.get(DIRECTORY_ID_KEY);
    if (passedDirectory != null) {
      directoryId.setValue(passedDirectory);
    }

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
    if (existingTask.deadlineMillis() != -1) {
      timeMillis.setValue(existingTask.deadlineMillis());
    }
    directoryId.setValue(existingTask.directoryId());
    repeatable.setValue(Optional.fromNullable(existingTask.repeatability()));
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

  public Optional<TaskRepeatability> getTaskRepeatability() {
    return repeatable.getValue();
  }

  public LiveData<String> getRepeatableString() {
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
    return new ClearRepeatableVisible(repeatable, taskType);
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

  private static long midnightTonight(Clock clock) {
    // 11:59 PM
    return DateUtils.midnightTonightMillis(clock) - TimeUnit.MINUTES.toMillis(1);
  }

  public ZonedDateTime getZoneDateTime() {
    return Instant.ofEpochMilli(timeMillis.getValue()).atZone(ZoneId.systemDefault());
  }

  public long getStartTimeMillis() {
    return timeMillis.getValue();
  }

  public void setDate(int year, int month, int dayOfMonth) {
    timeMillis.setValue(
        getZoneDateTime()
            .withYear(year)
            .withMonth(month + 1)
            .withDayOfMonth(dayOfMonth)
            .toInstant()
            .toEpochMilli());
  }

  public void setTime(int hourOfDay, int minute) {
    timeMillis.setValue(
        getZoneDateTime().withHour(hourOfDay).withMinute(minute).toInstant().toEpochMilli());
  }

  public void saveTask(String title, String notes) {
    this.title.setValue(title);
    this.notes.setValue(notes);
    Task.Builder taskBuilder =
        Task.builder()
            .setClock(clock)
            .setTitle(title)
            .setNotes(notes)
            .setDeadlineMillis(taskType.getValue() == Type.UNSCHEDULED ? -1 : timeMillis.getValue())
            .setRepeatability(
                taskType.getValue() == Type.REMINDER ? repeatable.getValue().orNull() : null)
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

  private static final class ClearRepeatableVisible
      extends MergerLiveData<Boolean, Optional<TaskRepeatability>, Task.Type, Void> {

    public ClearRepeatableVisible(
        LiveData<Optional<TaskRepeatability>> source1, LiveData<Type> source2) {
      super(source1, source2);
    }

    @Override
    protected Boolean onChanged() {
      return getSource1().isPresent() && getSource2().equals(Type.REMINDER);
    }

    @Override
    public boolean areEqual(Boolean val1, Boolean val2) {
      return val1 == val2;
    }
  }
}

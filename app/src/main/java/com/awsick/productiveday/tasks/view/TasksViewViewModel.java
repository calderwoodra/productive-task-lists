package com.awsick.productiveday.tasks.view;

import static com.awsick.productiveday.common.utils.ImmutableUtils.toImmutableList;

import androidx.hilt.lifecycle.ViewModelInject;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.awsick.productiveday.directories.models.DirectoryReference;
import com.awsick.productiveday.directories.repo.DirectoryReferenceRepo;
import com.awsick.productiveday.network.RequestStatus;
import com.awsick.productiveday.network.RequestStatus.Status;
import com.awsick.productiveday.tasks.models.Task;
import com.awsick.productiveday.tasks.models.Task.Type;
import com.awsick.productiveday.tasks.models.ViewTask;
import com.awsick.productiveday.tasks.repo.TasksRepo;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class TasksViewViewModel extends ViewModel {

  public enum TaskFilter {
    COMPLETED,
    TASKS,
    REMINDERS,
    DUE_TODAY,
    PAST_DUE,
    DUE_LATER,
    PIPE_DREAMS,
  }

  private final ImmutableMap<TaskFilter, ImmutableList<TaskFilter>> exclusionList =
      ImmutableMap.<TaskFilter, ImmutableList<TaskFilter>>builder()
          .put(TaskFilter.COMPLETED, ImmutableList.of())
          .put(TaskFilter.TASKS, ImmutableList.of(TaskFilter.REMINDERS, TaskFilter.PIPE_DREAMS))
          .put(TaskFilter.REMINDERS, ImmutableList.of(TaskFilter.TASKS, TaskFilter.PIPE_DREAMS))
          .put(TaskFilter.PIPE_DREAMS, ImmutableList.of(TaskFilter.REMINDERS, TaskFilter.TASKS))
          .put(TaskFilter.DUE_TODAY, ImmutableList.of(TaskFilter.PAST_DUE, TaskFilter.DUE_LATER))
          .put(TaskFilter.PAST_DUE, ImmutableList.of(TaskFilter.DUE_TODAY, TaskFilter.DUE_LATER))
          .put(TaskFilter.DUE_LATER, ImmutableList.of(TaskFilter.PAST_DUE, TaskFilter.DUE_TODAY))
          .build();

  private final ViewTasksLiveData viewTasksLiveData;
  private final MutableLiveData<ImmutableList<TaskFilter>> selectedFilters =
      new MutableLiveData<>(ImmutableList.of());
  private final MutableLiveData<ImmutableList<TaskFilter>> selectableFilters =
      new MutableLiveData<>(ImmutableList.copyOf(TaskFilter.values()));

  @ViewModelInject
  TasksViewViewModel(TasksRepo tasksRepo, DirectoryReferenceRepo directoryReferenceRepo) {
    viewTasksLiveData = new ViewTasksLiveData(tasksRepo, directoryReferenceRepo, selectedFilters);
  }

  public LiveData<RequestStatus<ImmutableList<ViewTask>>> getTasks() {
    return viewTasksLiveData;
  }

  public LiveData<ImmutableList<TaskFilter>> getSelectableFilters() {
    return selectableFilters;
  }

  public void selectFilter(TaskFilter filter, boolean isSelected) {
    ImmutableList<TaskFilter> selected = selectedFilters.getValue();
    if (isSelected) {
      selectedFilters.setValue(
          ImmutableList.<TaskFilter>builder().addAll(selected).add(filter).build());

      selectableFilters.setValue(
          selectableFilters.getValue().stream()
              .filter(f -> !exclusionList.get(filter).contains(f))
              .collect(toImmutableList()));
    } else {
      selectedFilters.setValue(
          selected.stream().filter(f -> !f.equals(filter)).collect(toImmutableList()));

      selectableFilters.setValue(
          ImmutableList.<TaskFilter>builder()
              .addAll(selectableFilters.getValue())
              .addAll(exclusionList.get(filter))
              .build());
    }
  }

  private static final class ViewTasksLiveData
      extends MediatorLiveData<RequestStatus<ImmutableList<ViewTask>>> {

    private final HashSet<LiveData<?>> directorySources = new HashSet<>();
    private final HashMap<Integer, RequestStatus<DirectoryReference>> directories = new HashMap<>();

    private RequestStatus<ImmutableList<Task>> tasks;
    private ImmutableList<TaskFilter> filters;

    ViewTasksLiveData(
        TasksRepo tasksRepo,
        DirectoryReferenceRepo directoryReferenceRepo,
        LiveData<ImmutableList<TaskFilter>> taskFilters) {
      setValue(RequestStatus.pending());

      // Watch for changes in task filters and apply them to the task list
      filters = taskFilters.getValue();
      addSource(taskFilters, this::applyFilters);

      // Add all incomplete tasks fetch as a source
      addSource(
          tasksRepo.getAllTasks(),
          tasks -> {
            // Each time the task list updates, set the status to pending, remove all directory
            // sources and start fetching each directory again.
            setValue(RequestStatus.pending());
            directorySources.stream().forEach(this::removeSource);
            this.tasks = tasks;
            if (tasks.status != Status.SUCCESS) {
              setValue(RequestStatus.pending());
              return;
            }

            tasks.getResult().stream().map(Task::directoryId).collect(Collectors.toSet()).stream()
                .forEach(
                    id -> {
                      // For each directory id, add the directory fetch as a source and track them
                      // so
                      // we can remove them later.
                      LiveData<RequestStatus<DirectoryReference>> source =
                          directoryReferenceRepo.getDirectory(id);
                      directorySources.add(source);

                      // As each directory loads, check to see if they've all loaded and update this
                      // LiveData's status to success with a list of ViewTasks.
                      addSource(
                          source,
                          directory -> {
                            directories.put(id, directory);
                            onDirectoryLoaded();
                          });
                    });
          });
    }

    private void onDirectoryLoaded() {
      if (tasks.status != Status.SUCCESS) {
        return;
      }

      if (tasks.getResult().stream()
          .map(Task::directoryId)
          .anyMatch(id -> !directories.containsKey(id))) {
        // Directory is not present
        return;
      }

      boolean anyFailures = false;
      boolean anyPending = false;
      for (RequestStatus<DirectoryReference> directory : directories.values()) {
        anyFailures |= directory.status == Status.FAILED;
        anyPending |= directory.status == Status.PENDING;
      }

      if (anyFailures) {
        if (getValue().status != Status.FAILED) {
          setValue(RequestStatus.error(new RuntimeException("Failed to load all directories.")));
        }
        return;
      }

      if (anyPending) {
        // we're already pending.
        return;
      }

      // All is good!
      setValue(RequestStatus.success(getTasks(directories, tasks.getResult(), filters)));
    }

    private void applyFilters(ImmutableList<TaskFilter> filters) {
      this.filters = filters;
      if (getValue() == null || getValue().status != Status.SUCCESS) {
        // There aren't any tasks we can apply the filters to
        return;
      }
      setValue(RequestStatus.success(getTasks(directories, tasks.getResult(), filters)));
    }

    private static ImmutableList<ViewTask> getTasks(
        HashMap<Integer, RequestStatus<DirectoryReference>> directories,
        ImmutableList<Task> tasks,
        ImmutableList<TaskFilter> filters) {
      Stream<ViewTask> stream =
          tasks.stream()
              .map(task -> new ViewTask(task, directories.get(task.directoryId()).getResult()));

      for (TaskFilter filter : filters) {
        switch (filter) {
          case COMPLETED:
            stream = stream.filter(task -> task.task.completed());
            break;
          case TASKS:
            stream = stream.filter(task -> task.task.type() == Type.DEADLINE);
            break;
          case REMINDERS:
            stream = stream.filter(task -> task.task.type() == Type.REMINDER);
            break;
          case DUE_TODAY:
            stream =
                stream
                    .filter(task -> task.task.deadlineMillis() != -1)
                    .filter(task -> task.task.getDaysFromToday() == 0);
            break;
          case PAST_DUE:
            stream =
                stream
                    .filter(task -> task.task.deadlineMillis() != -1)
                    .filter(task -> task.task.getDaysFromToday() < 0);
            break;
          case DUE_LATER:
            stream =
                stream
                    .filter(task -> task.task.deadlineMillis() != -1)
                    .filter(task -> task.task.getDaysFromToday() > 0);
            break;
          case PIPE_DREAMS:
            stream = stream.filter(task -> task.task.type() == Type.UNSCHEDULED);
            break;
          default:
            throw new IllegalArgumentException("Unhandled filter: " + filter);
        }
      }

      if (!filters.contains(TaskFilter.COMPLETED)) {
        stream = stream.filter(task -> !task.task.completed());
      }

      return stream
          .sorted(
              (task1, task2) -> {
                int completed = Boolean.compare(task1.task.completed(), task2.task.completed());
                if (completed != 0) {
                  return completed;
                }
                return Long.compare(task2.task.lastUpdated(), task1.task.lastUpdated());
              })
          .collect(toImmutableList());
    }
  }
}

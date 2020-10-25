package com.awsick.productiveday.tasks.view;

import static com.awsick.productiveday.common.utils.ImmutableUtils.toImmutableList;

import androidx.hilt.lifecycle.ViewModelInject;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;
import com.awsick.productiveday.directories.models.DirectoryReference;
import com.awsick.productiveday.directories.repo.DirectoryReferenceRepo;
import com.awsick.productiveday.network.RequestStatus;
import com.awsick.productiveday.network.RequestStatus.Status;
import com.awsick.productiveday.tasks.models.Task;
import com.awsick.productiveday.tasks.models.ViewTask;
import com.awsick.productiveday.tasks.repo.TasksRepo;
import com.google.common.collect.ImmutableList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.stream.Collectors;

public final class TasksViewViewModel extends ViewModel {

  private final ViewTasksLiveData viewTasksLiveData;

  @ViewModelInject
  TasksViewViewModel(TasksRepo tasksRepo, DirectoryReferenceRepo directoryReferenceRepo) {
    viewTasksLiveData = new ViewTasksLiveData(tasksRepo, directoryReferenceRepo);
  }

  public LiveData<RequestStatus<ImmutableList<ViewTask>>> getTasks() {
    return viewTasksLiveData;
  }

  private static final class ViewTasksLiveData
      extends MediatorLiveData<RequestStatus<ImmutableList<ViewTask>>> {

    private final HashSet<LiveData<?>> directorySources = new HashSet<>();
    private final HashMap<Integer, RequestStatus<DirectoryReference>> directories = new HashMap<>();

    private RequestStatus<ImmutableList<Task>> tasks;

    ViewTasksLiveData(TasksRepo tasksRepo, DirectoryReferenceRepo directoryReferenceRepo) {
      setValue(RequestStatus.pending());

      // Add all incomplete tasks fetch as a source
      addSource(
          tasksRepo.getIncompleteTasks(),
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
      setValue(
          RequestStatus.success(
              tasks.getResult().stream()
                  .map(task -> new ViewTask(task, directories.get(task.directoryId()).getResult()))
                  .collect(toImmutableList())));
    }
  }
}

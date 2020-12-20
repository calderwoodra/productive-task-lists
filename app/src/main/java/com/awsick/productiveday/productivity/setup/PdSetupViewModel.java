package com.awsick.productiveday.productivity.setup;

import static com.awsick.productiveday.common.utils.ImmutableUtils.toImmutableList;

import androidx.hilt.Assisted;
import androidx.hilt.lifecycle.ViewModelInject;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;
import com.awsick.productiveday.R;
import com.awsick.productiveday.common.viewmodelutils.MergerLiveData;
import com.awsick.productiveday.network.RequestStatus;
import com.awsick.productiveday.network.RequestStatus.Status;
import com.awsick.productiveday.tasks.models.Task;
import com.awsick.productiveday.tasks.repo.TasksRepo;
import com.google.common.collect.ImmutableList;

public final class PdSetupViewModel extends ViewModel {

  private final MutableLiveData<ImmutableList<Task>> inputTasks =
      new MutableLiveData<>(ImmutableList.of());

  private final TasksRepo tasksRepo;
  private final TasksLiveData tasks;

  @ViewModelInject
  PdSetupViewModel(TasksRepo tasksRepo, @Assisted SavedStateHandle savedState) {
    this.tasksRepo = tasksRepo;
    tasks = new TasksLiveData(inputTasks, tasksRepo.getIncompleteTasks());
  }

  public void addTask(Task task) {
    inputTasks.setValue(
        ImmutableList.<Task>builder().addAll(inputTasks.getValue()).add(task).build());
  }

  public void removeTask(Task task) {
    inputTasks.setValue(
        inputTasks.getValue().stream().filter(t -> !t.equals(task)).collect(toImmutableList()));
  }

  public void saveTasks() {
    ImmutableList<Task> tasks = inputTasks.getValue();
    inputTasks.setValue(ImmutableList.of());
    tasksRepo.createTasks(tasks);
    // TODO(allen): set consumable
    // TODO(allen): launch screen to sort tasks
  }

  public LiveData<RequestStatus<ImmutableList<Task>>> getTasks() {
    return tasks;
  }

  public LiveData<Integer> getEditTextHint() {
    return Transformations.map(
        tasks,
        tasks -> {
          if (tasks.status != Status.SUCCESS || tasks.getResult().isEmpty()) {
            return R.string.first_task_hint;
          }
          return R.string.nth_task_hint;
        });
  }

  private static final class TasksLiveData
      extends MergerLiveData<
          RequestStatus<ImmutableList<Task>>,
          ImmutableList<Task>,
          RequestStatus<ImmutableList<Task>>,
          Void> {

    public TasksLiveData(
        LiveData<ImmutableList<Task>> inputTasks,
        LiveData<RequestStatus<ImmutableList<Task>>> existingTasks) {
      super(inputTasks, existingTasks);
    }

    @Override
    protected RequestStatus<ImmutableList<Task>> onChanged() {
      if (getSource2().status != Status.SUCCESS) {
        return getSource2();
      }

      return RequestStatus.success(
          ImmutableList.<Task>builder()
              .addAll(getSource1().reverse())
              .addAll(
                  getSource2().getResult().stream()
                      .filter(task -> task.deadlineMillis() > 0)
                      .filter(task -> task.getDaysFromToday() <= 0)
                      .collect(toImmutableList()))
              .build());
    }

    @Override
    public boolean areEqual(
        RequestStatus<ImmutableList<Task>> val1, RequestStatus<ImmutableList<Task>> val2) {
      return false;
    }
  }
}

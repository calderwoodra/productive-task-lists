package com.awsick.productiveday.productivity.setup;

import static com.awsick.productiveday.common.utils.ImmutableUtils.toImmutableList;

import androidx.hilt.Assisted;
import androidx.hilt.lifecycle.ViewModelInject;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import com.google.common.collect.ImmutableList;

public final class PdSetupViewModel extends ViewModel {

  private final MutableLiveData<ImmutableList<String>> tasks =
      new MutableLiveData<>(ImmutableList.of());

  @ViewModelInject
  PdSetupViewModel(@Assisted SavedStateHandle savedState) {}

  public void addTask(String title) {
    tasks.setValue(ImmutableList.<String>builder().addAll(tasks.getValue()).add(title).build());
  }

  public void removeTask(String title) {
    tasks.setValue(
        tasks.getValue().stream().filter(task -> !task.equals(title)).collect(toImmutableList()));
  }

  public LiveData<ImmutableList<String>> getTasks() {
    return tasks;
  }
}

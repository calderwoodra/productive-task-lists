package com.awsick.productiveday.tasks;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.awsick.productiveday.R;
import com.awsick.productiveday.network.RequestStatus.Status;
import com.awsick.productiveday.tasks.repo.TasksRepo;
import dagger.hilt.android.AndroidEntryPoint;
import javax.inject.Inject;

@AndroidEntryPoint
public final class TasksLoadingFragment extends Fragment {

  @Inject
  TasksRepo tasksRepo;

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_loading_tasks, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View root, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(root, savedInstanceState);
    tasksRepo.getIncompleteTasks().observe(getViewLifecycleOwner(), tasks -> {
      Log.i("###", "tasks status: " + tasks.status);
      if (tasks.status != Status.SUCCESS) {
        // No-op
        return;
      }
      Log.i("###", "tasks available");
      Navigation.findNavController(root).navigate(R.id.action_tasks_loading_complete);
    });
  }
}

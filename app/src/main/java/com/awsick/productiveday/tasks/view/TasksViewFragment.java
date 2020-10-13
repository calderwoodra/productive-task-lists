package com.awsick.productiveday.tasks.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.awsick.productiveday.R;
import com.awsick.productiveday.tasks.repo.TasksRepo;
import dagger.hilt.android.AndroidEntryPoint;
import javax.inject.Inject;

@AndroidEntryPoint
public final class TasksViewFragment extends Fragment {

  @Inject
  TasksRepo tasksRepo;

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_view_tasks, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View root, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(root, savedInstanceState);
    root.findViewById(R.id.fab)
        .setOnClickListener(view ->
            Navigation.findNavController(root).navigate(R.id.action_create_task));
  }
}

package com.awsick.productiveday.tasks.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.awsick.productiveday.R;
import com.awsick.productiveday.tasks.models.Task;
import com.awsick.productiveday.tasks.repo.TasksRepo;
import com.awsick.productiveday.tasks.view.TaskListAdapter.TaskItemActionListener;
import com.google.android.material.snackbar.Snackbar;
import dagger.hilt.android.AndroidEntryPoint;
import javax.inject.Inject;

@AndroidEntryPoint
public final class TasksViewFragment extends Fragment {

  @Inject TasksRepo tasksRepo;

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
        .setOnClickListener(
            view -> Navigation.findNavController(root).navigate(R.id.action_create_task));

    TasksViewViewModel viewModel = new ViewModelProvider(this).get(TasksViewViewModel.class);

    RecyclerView rv = root.findViewById(R.id.task_list);
    rv.setLayoutManager(new LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false));
    TaskListAdapter adapter = new TaskListAdapter(new TaskActionListener(tasksRepo));
    rv.setAdapter(adapter);
    viewModel
        .getTasks()
        .observe(
            getViewLifecycleOwner(),
            tasks -> {
              switch (tasks.status) {
                case INITIAL:
                  // No-op
                  break;
                case PENDING:
                  // TODO(allen): Show loading indicator
                  break;
                case SUCCESS:
                  adapter.setTasks(tasks.getResult());
                  break;
                case FAILED:
                  // TODO(allen): Show try again action
                  Snackbar.make(root, "Something went wrong", Snackbar.LENGTH_INDEFINITE).show();
                  break;
              }
            });
  }

  private static final class TaskActionListener implements TaskItemActionListener {

    private final TasksRepo repo;

    private TaskActionListener(TasksRepo repo) {
      this.repo = repo;
    }

    @Override
    public void onCompleteTaskRequested(Task task) {
      repo.markTaskCompleted(task);
    }

    @Override
    public void onEditTaskRequested(Task task) {
      // TODO(allen): navigate to create task fragment with data prepopulate
    }
  }
}

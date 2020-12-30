package com.awsick.productiveday.tasks.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver;
import com.awsick.productiveday.R;
import com.awsick.productiveday.common.uiutils.FragmentUtils;
import com.awsick.productiveday.main.MainParentContainer;
import com.awsick.productiveday.tasks.create.TaskCreateActivity;
import com.awsick.productiveday.tasks.models.Task;
import com.awsick.productiveday.tasks.repo.TasksRepo;
import com.awsick.productiveday.tasks.view.TaskListAdapter.TaskItemActionListener;
import com.awsick.productiveday.tasks.view.TasksViewViewModel.TaskFilter;
import com.google.android.material.chip.Chip;
import com.google.android.material.snackbar.Snackbar;
import com.google.common.collect.ImmutableMap;
import dagger.hilt.android.AndroidEntryPoint;
import java.util.Map.Entry;
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
    MainParentContainer parent = FragmentUtils.getParentUnsafe(this, MainParentContainer.class);
    parent.setFabVisibility(true);
    parent.setFabOcl(view -> startActivity(TaskCreateActivity.create(requireContext())));
    parent.setToolbarTitle("Productive Task List");

    TasksViewViewModel viewModel = new ViewModelProvider(this).get(TasksViewViewModel.class);

    RecyclerView rv = root.findViewById(R.id.task_list);
    rv.setLayoutManager(new LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false));
    TaskListAdapter adapter = new TaskListAdapter(new TaskActionListener(tasksRepo));
    adapter.registerAdapterDataObserver(
        new AdapterDataObserver() {
          @Override
          public void onItemRangeInserted(int positionStart, int itemCount) {
            super.onItemRangeInserted(positionStart, itemCount);
            rv.smoothScrollToPosition(0);
          }
        });
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

    setupChips(root, viewModel);
  }

  private void setupChips(View root, TasksViewViewModel viewModel) {
    Chip completed = root.findViewById(R.id.task_chip_completed);
    Chip deadline = root.findViewById(R.id.task_chip_deadline);
    Chip reminder = root.findViewById(R.id.task_chip_reminder);
    Chip today = root.findViewById(R.id.task_chip_today);
    Chip past = root.findViewById(R.id.task_chip_past);
    Chip later = root.findViewById(R.id.task_chip_later);
    Chip pipeDream = root.findViewById(R.id.task_chip_unscheduled);

    ImmutableMap<TaskFilter, Chip> chipMap =
        ImmutableMap.<TaskFilter, Chip>builder()
            .put(TaskFilter.COMPLETED, completed)
            .put(TaskFilter.TASKS, deadline)
            .put(TaskFilter.REMINDERS, reminder)
            .put(TaskFilter.DUE_TODAY, today)
            .put(TaskFilter.PAST_DUE, past)
            .put(TaskFilter.DUE_LATER, later)
            .put(TaskFilter.PIPE_DREAMS, pipeDream)
            .build();

    for (Entry<TaskFilter, Chip> entry : chipMap.entrySet()) {
      entry
          .getValue()
          .setOnCheckedChangeListener(
              (buttonView, isChecked) -> viewModel.selectFilter(entry.getKey(), isChecked));
    }

    viewModel
        .getSelectableFilters()
        .observe(
            getViewLifecycleOwner(),
            filters -> {
              for (Entry<TaskFilter, Chip> entry : chipMap.entrySet()) {
                entry
                    .getValue()
                    .setVisibility(filters.contains(entry.getKey()) ? View.VISIBLE : View.GONE);
              }
            });
  }

  private final class TaskActionListener implements TaskItemActionListener {

    private final TasksRepo repo;

    private TaskActionListener(TasksRepo repo) {
      this.repo = repo;
    }

    @Override
    public void onCompleteTaskRequested(Task task) {
      repo.markTaskCompleted(task);
    }

    @Override
    public void onUncompleteTaskRequested(Task task) {
      repo.markTaskIncomplete(task);
    }

    @Override
    public void onEditTaskRequested(Task task) {
      startActivity(TaskCreateActivity.create(requireContext(), task));
    }
  }
}

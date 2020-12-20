package com.awsick.productiveday.productivity.setup;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver;
import com.awsick.productiveday.R;
import com.awsick.productiveday.common.uiutils.FragmentUtils;
import com.awsick.productiveday.common.utils.DateUtils;
import com.awsick.productiveday.main.MainParentContainer;
import com.awsick.productiveday.productivity.setup.PdSetupTaskAdapter.TaskActions;
import com.awsick.productiveday.tasks.create.TaskCreateActivity;
import com.awsick.productiveday.tasks.models.Task;
import com.awsick.productiveday.tasks.models.Task.Type;
import com.google.common.base.Strings;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public final class ProductivitySetupFragment extends Fragment {

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_productivity_setup, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View root, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(root, savedInstanceState);
    MainParentContainer parent = FragmentUtils.getParentUnsafe(this, MainParentContainer.class);
    parent.setToolbarTitle("Productive Day");
    parent.setFabVisibility(false);

    PdSetupViewModel viewModel = new ViewModelProvider(this).get(PdSetupViewModel.class);
    RecyclerView rv = root.findViewById(R.id.pd_setup_tasks);
    rv.setLayoutManager(new LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false));

    TaskActionsImpl taskActions = new TaskActionsImpl(viewModel);
    PdSetupTaskAdapter adapter = new PdSetupTaskAdapter(taskActions);
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
                case PENDING:
                  break;
                case FAILED:
                  // TODO(allen): Show an error
                  break;
                case SUCCESS:
                  adapter.setData(tasks.getResult());
                  hideEmptyState(!tasks.getResult().isEmpty(), root);
                  showSaveButton(
                      tasks.getResult().stream().anyMatch(task -> task.uid() == 0), root);
                  break;
              }
            });

    EditText taskInput = root.findViewById(R.id.pd_setup_task_input);
    viewModel.getEditTextHint().observe(getViewLifecycleOwner(), taskInput::setHint);
    root.findViewById(R.id.pd_setup_create_task)
        .setOnClickListener(
            view -> {
              String input = taskInput.getText().toString();
              if (!Strings.isNullOrEmpty(input)) {
                viewModel.addTask(
                    Task.builder()
                        .setTitle(input)
                        .setType(Type.DEADLINE)
                        .setDeadlineMillis(DateUtils.midnightTonightMillis())
                        .build());
                taskInput.setText("");
              }
            });

    root.findViewById(R.id.task_input_complete).setOnClickListener(view -> viewModel.saveTasks());
  }

  private static void hideEmptyState(boolean hide, View root) {
    root.findViewById(R.id.pd_setup_title).setVisibility(hide ? View.GONE : View.VISIBLE);
    root.findViewById(R.id.pd_setup_body).setVisibility(hide ? View.GONE : View.VISIBLE);
  }

  private static void showSaveButton(boolean hide, View root) {
    root.findViewById(R.id.task_input_complete).setVisibility(hide ? View.VISIBLE : View.INVISIBLE);
  }

  private final class TaskActionsImpl implements TaskActions {

    private final PdSetupViewModel viewModel;

    private TaskActionsImpl(PdSetupViewModel viewModel) {
      this.viewModel = viewModel;
    }

    @Override
    public void markTaskCompleted(Task task) {
      viewModel.markTaskComplete(task);
    }

    @Override
    public void editTask(Task task) {
      startActivity(TaskCreateActivity.create(requireContext(), task));
    }

    @Override
    public void removeTask(Task task) {
      viewModel.removeTask(task);
    }
  }
}

package com.awsick.productiveday.tasks.create;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import com.awsick.productiveday.R;
import com.awsick.productiveday.network.RequestStatus.Status;
import com.awsick.productiveday.tasks.models.Task;
import com.awsick.productiveday.tasks.models.Task.Type;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.snackbar.Snackbar;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public final class TasksCreateFragment extends Fragment {

  static final String TASK_ID_KEY = "TASK_ID";

  public static TasksCreateFragment create(int taskId) {
    TasksCreateFragment fragment = new TasksCreateFragment();
    Bundle args = new Bundle();
    args.putInt(TASK_ID_KEY, taskId);
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    View root = inflater.inflate(R.layout.fragment_create_task, container, false);
    Toolbar toolbar = root.findViewById(R.id.toolbar);
    toolbar.setNavigationOnClickListener(view -> requireActivity().finish());
    toolbar.setTitle(getArguments().getInt(TASK_ID_KEY, -1) == -1 ? "Create Task" : "Update Task");
    return root;
  }

  @Override
  public void onViewCreated(@NonNull View root, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(root, savedInstanceState);
    TasksCreateViewModel viewModel = new ViewModelProvider(this).get(TasksCreateViewModel.class);

    EditText titleEt = root.findViewById(R.id.create_task_title);
    EditText notesEt = root.findViewById(R.id.create_task_notes);
    setupTitleAndNotes(viewModel, titleEt, notesEt);

    observe(viewModel.getDate(), R.id.create_task_deadline_date);
    observe(viewModel.getTime(), R.id.create_task_deadline_time);
    observe(viewModel.getRepeatable(), R.id.create_task_repeat);
    TextView directoryName = root.findViewById(R.id.create_task_directory);
    viewModel
        .getDirectoryName()
        .observe(
            getViewLifecycleOwner(),
            name -> {
              if (name.status != Status.SUCCESS) {
                return;
              }
              directoryName.setText(name.getResult());
            });

    root.findViewById(R.id.task_create_save)
        .setOnClickListener(
            view -> viewModel.saveTask(titleEt.getText().toString(), notesEt.getText().toString()));

    viewModel
        .getSaveEvents()
        .observe(
            getViewLifecycleOwner(),
            event -> {
              switch (event) {
                case SUCCESSFULLY_SAVED:
                  requireActivity().finish();
                  break;
                case FAILED_TO_SAVE:
                  Snackbar.make(root, "Something went wrong", Snackbar.LENGTH_INDEFINITE)
                      .setAction(
                          "Try again",
                          view ->
                              viewModel.saveTask(
                                  titleEt.getText().toString(), notesEt.getText().toString()))
                      .show();
                  break;
                case TITLE_MISSING:
                  Snackbar.make(root, "Missing title", Snackbar.LENGTH_SHORT).show();
                  break;
              }
            });

    setupChips(viewModel, root);
    // TODO(allen): Setup click listener for date
    // TODO(allen): Setup click listener for time
    // TODO(allen): Setup click listener for repeatability
    // TODO(allen): Setup click listener for category
  }

  private void setupTitleAndNotes(
      TasksCreateViewModel viewModel, EditText titleEt, EditText notesEt) {
    viewModel
        .getTitle()
        .observe(
            getViewLifecycleOwner(),
            title -> {
              titleEt.setText(title);
              titleEt.setSelection(title.length());
            });

    viewModel
        .getNotes()
        .observe(
            getViewLifecycleOwner(),
            notes -> {
              notesEt.setText(notes);
              notesEt.setSelection(notes.length());
            });
  }

  private void setupChips(TasksCreateViewModel viewModel, View root) {
    ChipGroup chipGroup = root.findViewById(R.id.task_type_chip_group);
    chipGroup.setOnCheckedChangeListener(
        (group, checkedId) -> viewModel.setTaskType(toTaskType(checkedId)));

    viewModel
        .getTaskType()
        .observe(getViewLifecycleOwner(), type -> chipGroup.check(toChipId(type)));

    View v1 = root.findViewById(R.id.create_task_repeat_icon);
    View v2 = root.findViewById(R.id.create_task_repeat);
    View v3 = root.findViewById(R.id.create_task_deadline_icon);
    View v4 = root.findViewById(R.id.create_task_deadline_date);
    View v5 = root.findViewById(R.id.create_task_deadline_time);
    viewModel
        .schedulingVisible()
        .observe(
            getViewLifecycleOwner(),
            visible -> {
              int visibility = visible ? View.VISIBLE : View.GONE;
              v1.setVisibility(visibility);
              v2.setVisibility(visibility);
              v3.setVisibility(visibility);
              v4.setVisibility(visibility);
              v5.setVisibility(visibility);
            });
  }

  private static Task.Type toTaskType(@IdRes int chipId) {
    if (chipId == R.id.task_chip_deadline) {
      return Type.DEADLINE;
    } else if (chipId == R.id.task_chip_reminder) {
      return Type.REMINDER;
    } else if (chipId == R.id.task_chip_unscheduled) {
      return Type.UNSCHEDULED;
    } else {
      throw new IllegalArgumentException("Unknown chip id: " + chipId);
    }
  }

  @IdRes
  private static int toChipId(Task.Type type) {
    switch (type) {
      case DEADLINE:
        return R.id.task_chip_deadline;
      case REMINDER:
        return R.id.task_chip_reminder;
      case UNSCHEDULED:
        return R.id.task_chip_unscheduled;
    }
    throw new IllegalArgumentException("Unhandled type: " + type);
  }

  private void observe(LiveData<String> string, @IdRes int viewId) {
    string.observe(
        getViewLifecycleOwner(),
        text -> ((TextView) requireView().findViewById(viewId)).setText(text));
  }
}

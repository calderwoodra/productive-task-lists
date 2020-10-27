package com.awsick.productiveday.tasks.create;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import com.awsick.productiveday.R;
import com.awsick.productiveday.network.RequestStatus.Status;
import com.awsick.productiveday.tasks.models.Task;
import com.awsick.productiveday.tasks.models.Task.Type;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.snackbar.Snackbar;
import dagger.hilt.android.AndroidEntryPoint;
import java.util.Calendar;

@AndroidEntryPoint
public final class TasksCreateFragment extends Fragment {

  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    setArguments(
        requireActivity().getIntent().getExtras() == null
            ? new Bundle()
            : requireActivity().getIntent().getExtras());
    return inflater.inflate(R.layout.fragment_create_task, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View root, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(root, savedInstanceState);
    TasksCreateViewModel viewModel =
        new ViewModelProvider(requireActivity()).get(TasksCreateViewModel.class);

    EditText titleEt = root.findViewById(R.id.create_task_title);
    EditText notesEt = root.findViewById(R.id.create_task_notes);
    setupTitleAndNotes(viewModel, titleEt, notesEt);

    observe(viewModel.getDate(), R.id.create_task_deadline_date);
    observe(viewModel.getTime(), R.id.create_task_deadline_time);
    observe(viewModel.getRepeatable(), R.id.create_task_repeat);
    TextView directoryName = root.findViewById(R.id.create_task_directory);
    directoryName.setOnClickListener(
        view -> new SelectDirectoryDialogFragment().showNow(getChildFragmentManager(), null));
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
    setupDate(viewModel, root);
    setupTime(viewModel, root);
    setupRepeat(viewModel, root);
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

    View v3 = root.findViewById(R.id.create_task_deadline_icon);
    View v4 = root.findViewById(R.id.create_task_deadline_date);
    View v5 = root.findViewById(R.id.create_task_deadline_time);
    viewModel
        .schedulingVisible()
        .observe(
            getViewLifecycleOwner(),
            visible -> {
              int visibility = visible ? View.VISIBLE : View.GONE;
              v3.setVisibility(visibility);
              v4.setVisibility(visibility);
              v5.setVisibility(visibility);
            });

    View v1 = root.findViewById(R.id.create_task_repeat_icon);
    View v2 = root.findViewById(R.id.create_task_repeat);
    viewModel
        .repeatVisible()
        .observe(
            getViewLifecycleOwner(),
            visible -> {
              int visibility = visible ? View.VISIBLE : View.GONE;
              v1.setVisibility(visibility);
              v2.setVisibility(visibility);
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

  private void setupDate(TasksCreateViewModel viewModel, View root) {
    Calendar calendar = viewModel.getCalendar();
    root.findViewById(R.id.create_task_deadline_date)
        .setOnClickListener(
            view ->
                new DatePickerDialog(
                        requireContext(),
                        (picker, year, month, dayOfMonth) ->
                            viewModel.setDate(year, month, dayOfMonth),
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH))
                    .show());
  }

  private void setupTime(TasksCreateViewModel viewModel, View root) {
    Calendar calendar = viewModel.getCalendar();
    root.findViewById(R.id.create_task_deadline_time)
        .setOnClickListener(
            view ->
                new TimePickerDialog(
                        requireContext(),
                        (view1, hourOfDay, minute) -> viewModel.setTime(hourOfDay, minute),
                        calendar.get(Calendar.HOUR_OF_DAY),
                        calendar.get(Calendar.MINUTE),
                        false)
                    .show());
  }

  private void setupRepeat(TasksCreateViewModel viewModel, View root) {
    TextView repeat = root.findViewById(R.id.create_task_repeat);
    repeat.setOnClickListener(
        view ->
            Navigation.findNavController(root)
                .navigate(R.id.action_tasksCreateFragment_to_taskRepeatFragment));
    viewModel.getRepeatable().observe(getViewLifecycleOwner(), repeat::setText);
  }
}

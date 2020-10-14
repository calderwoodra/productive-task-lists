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
import androidx.navigation.Navigation;
import com.awsick.productiveday.R;
import com.google.android.material.snackbar.Snackbar;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public final class TasksCreateFragment extends Fragment {

  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    View root = inflater.inflate(R.layout.fragment_create_task, container, false);
    ((Toolbar) root.findViewById(R.id.toolbar))
        .setNavigationOnClickListener(view -> Navigation.findNavController(root).popBackStack());
    return root;
  }

  @Override
  public void onViewCreated(@NonNull View root, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(root, savedInstanceState);
    TasksCreateViewModel viewModel = new ViewModelProvider(this).get(TasksCreateViewModel.class);

    EditText titleEt = root.findViewById(R.id.create_task_title);
    viewModel.getTitle().observe(getViewLifecycleOwner(), title -> {
      titleEt.setText(title);
      titleEt.setSelection(title.length());
    });

    EditText notesEt = root.findViewById(R.id.create_task_notes);
    viewModel.getNotes().observe(getViewLifecycleOwner(), notes -> {
      notesEt.setText(notes);
      notesEt.setSelection(notes.length());
    });

    observe(viewModel.getDate(), R.id.create_task_deadline_date);
    observe(viewModel.getTime(), R.id.create_task_deadline_time);
    observe(viewModel.getRepeatable(), R.id.create_task_repeat);
    observe(viewModel.getDirectoryName(), R.id.create_task_directory);

    root.findViewById(R.id.task_create_save).setOnClickListener(view -> {
      viewModel.saveTask(titleEt.getText().toString(), notesEt.getText().toString());
    });

    viewModel.getSaveEvents().observe(getViewLifecycleOwner(), event -> {
      switch (event) {
        case SUCCESSFULLY_SAVED:
          Navigation.findNavController(root).popBackStack();
          break;
        case FAILED_TO_SAVE:
          Snackbar.make(root, "Something went wrong", Snackbar.LENGTH_INDEFINITE)
              .setAction("Try again",
                  view -> viewModel.saveTask(
                      titleEt.getText().toString(), notesEt.getText().toString()))
              .show();
          break;
        case TITLE_MISSING:
          Snackbar.make(root, "Missing title", Snackbar.LENGTH_SHORT).show();
          break;
      }
    });

    // TODO(allen): Setup click listener for date
    // TODO(allen): Setup click listener for time
    // TODO(allen): Setup click listener for repeatability
    // TODO(allen): Setup click listener for category
  }

  private void observe(LiveData<String> string, @IdRes int viewId) {
    string.observe(getViewLifecycleOwner(),
        text -> ((TextView) requireView().findViewById(viewId)).setText(text));
  }
}

package com.awsick.productiveday.tasks.create;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.awsick.productiveday.R;
import com.awsick.productiveday.directories.models.DirectoryReference;
import com.awsick.productiveday.directories.repo.DirectoryRepo;
import com.awsick.productiveday.directories.ui.DirectoryListAdapter;
import com.awsick.productiveday.directories.ui.DirectoryListAdapter.DirectoryItemActionListener;
import com.awsick.productiveday.network.RequestStatus.Status;
import com.awsick.productiveday.tasks.models.Task;
import dagger.hilt.android.AndroidEntryPoint;
import javax.inject.Inject;

@AndroidEntryPoint
public final class SelectDirectoryDialogFragment extends DialogFragment {

  @Inject DirectoryRepo directoryRepo;

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.dialog_fragment_select_directory, container, false);
    requireDialog().setCanceledOnTouchOutside(false);
    return view;
  }

  @Override
  public void onViewCreated(@NonNull View root, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(root, savedInstanceState);
    TasksCreateViewModel viewModel =
        new ViewModelProvider(requireActivity()).get(TasksCreateViewModel.class);

    TextView title = root.findViewById(R.id.select_directory_title);
    Button selectButton = root.findViewById(R.id.select_directory_button);
    viewModel
        .getDirectoryName()
        .observe(
            getViewLifecycleOwner(),
            name -> {
              if (name.status != Status.SUCCESS) {
                return;
              }
              title.setText("Selected Directory: " + name.getResult());
              selectButton.setText("Select " + name.getResult());
            });

    RecyclerView rv = root.findViewById(R.id.select_directory_rv);
    rv.setLayoutManager(new LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false));
    DirectoryListAdapter adapter =
        new DirectoryListAdapter(new TaskDirectoryItemListener(viewModel), false);
    rv.setAdapter(adapter);

    viewModel
        .getDirectory()
        .observe(
            getViewLifecycleOwner(),
            directory -> {
              if (directory.status != Status.SUCCESS) {
                return;
              }
              adapter.setDirectory(directory.getResult());
            });

    int currentDirectory = viewModel.getCurrentDirectory();
    root.findViewById(R.id.dismiss_button)
        .setOnClickListener(
            view -> {
              viewModel.setDirectoryId(currentDirectory);
              dismiss();
            });
    selectButton.setOnClickListener(
        view -> {
          dismiss();
        });
  }

  private static final class TaskDirectoryItemListener implements DirectoryItemActionListener {

    private final TasksCreateViewModel viewModel;

    public TaskDirectoryItemListener(TasksCreateViewModel viewModel) {
      this.viewModel = viewModel;
    }

    @Override
    public void onNavigateToDirectory(DirectoryReference directory) {
      viewModel.setDirectoryId(directory.uid());
    }

    @Override
    public void onCompleteTaskRequested(Task task) {
      throw new UnsupportedOperationException(
          "When creating a task and choosing the directory that it will go in, "
              + "there shouldn't be any tasks visible.");
    }

    @Override
    public void onEditTaskRequested(Task task) {
      throw new UnsupportedOperationException(
          "When creating a task and choosing the directory that it will go in, "
              + "there shouldn't be any tasks visible.");
    }

    @Override
    public void onEditDirectoryRequested(DirectoryReference reference) {
      // Ignore, user should be selecting a directory instead
    }
  }
}

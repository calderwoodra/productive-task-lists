package com.awsick.productiveday.directories.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.awsick.productiveday.R;
import com.awsick.productiveday.common.uiutils.FragmentUtils;
import com.awsick.productiveday.directories.models.DirectoryReference;
import com.awsick.productiveday.directories.ui.DirectoryListAdapter.DirectoryItemActionListener;
import com.awsick.productiveday.main.MainParentContainer;
import com.awsick.productiveday.main.MainParentContainer.FabOptions;
import com.awsick.productiveday.network.RequestStatus.Status;
import com.awsick.productiveday.tasks.create.TaskCreateActivity;
import com.awsick.productiveday.tasks.models.Task;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public final class DirectoryBrowseFragment extends Fragment {

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_browse_directory, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View root, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(root, savedInstanceState);
    DirectoryBrowseViewModel viewModel =
        new ViewModelProvider(this).get(DirectoryBrowseViewModel.class);

    RecyclerView rv = root.findViewById(R.id.directory_list);
    rv.setLayoutManager(new LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false));
    DirectoryListAdapter adapter =
        new DirectoryListAdapter(new DirectoryItemListener(viewModel), true);
    rv.setAdapter(adapter);

    OnBackPressedCallback callback =
        new OnBackPressedCallback(!viewModel.isRootDirectory()) {
          @Override
          public void handleOnBackPressed() {
            viewModel.navigateUp();
          }
        };
    requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), callback);

    MainParentContainer parent = FragmentUtils.getParentUnsafe(this, MainParentContainer.class);
    parent.setFabVisibility(true);
    viewModel
        .getCurrentDirectory()
        .observe(
            getViewLifecycleOwner(),
            directory -> {
              if (directory.status != Status.SUCCESS) {
                return;
              }
              callback.setEnabled(!viewModel.isRootDirectory());
              parent.setToolbarTitle(directory.getResult().reference().name() + " Directory");
              parent.setupExpandableFab(
                  new FabOptions(
                      R.drawable.ic_baseline_notes_24,
                      fab1 -> {
                        // TODO(allen): create task in directory
                      }),
                  new FabOptions(
                      R.drawable.ic_baseline_folder_24,
                      fab2 -> {
                        int currentDirectory = directory.getResult().reference().uid();
                        CreateDirectoryDialogFragment.create(currentDirectory)
                            .show(getChildFragmentManager(), null);
                      }));
              adapter.setDirectory(directory.getResult());
            });
  }

  private final class DirectoryItemListener implements DirectoryItemActionListener {

    private final DirectoryBrowseViewModel viewModel;

    private DirectoryItemListener(DirectoryBrowseViewModel viewModel) {
      this.viewModel = viewModel;
    }

    @Override
    public void onNavigateToDirectory(DirectoryReference directory) {
      viewModel.setCurrentDirectory(directory.uid());
    }

    @Override
    public void onCompleteTaskRequested(Task task) {
      viewModel.markTaskCompleted(task);
    }

    @Override
    public void onEditTaskRequested(Task task) {
      startActivity(TaskCreateActivity.create(requireContext(), task));
    }

    @Override
    public void onEditDirectoryRequested(DirectoryReference reference) {
      CreateDirectoryDialogFragment.create(
              viewModel.getCurrentDirectory().getValue().getResult().reference().uid(), reference)
          .show(getChildFragmentManager(), null);
    }
  }
}

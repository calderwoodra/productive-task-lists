package com.awsick.productiveday.directories.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.awsick.productiveday.R;
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
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    DirectoryBrowseViewModel viewModel =
        new ViewModelProvider(this).get(DirectoryBrowseViewModel.class);
    viewModel
        .getCurrentDirectory()
        .observe(
            getViewLifecycleOwner(),
            directory -> {
              // TODO(allen): implement ui
            });
  }
}

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
import com.awsick.productiveday.R;
import com.awsick.productiveday.common.uiutils.FragmentUtils;
import com.awsick.productiveday.main.MainParentContainer;
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

    PdSetupTaskAdapter adapter = new PdSetupTaskAdapter();
    rv.setAdapter(adapter);
    viewModel.getTasks().observe(getViewLifecycleOwner(), adapter::setData);

    // TODO(allen): Consider removing title + blurb
    // TODO(allen): Consider adjust pan vs adjust resize
    // TODO(allen): update edit text hint along with task list
    // TODO(allen): What comes after inputting all tasks

    EditText taskInput = root.findViewById(R.id.pd_setup_task_input);
    root.findViewById(R.id.pd_setup_create_task)
        .setOnClickListener(
            view -> {
              String input = taskInput.getText().toString();
              if (!Strings.isNullOrEmpty(input)) {
                viewModel.addTask(input);
                taskInput.setText("");
              }
            });
  }
}

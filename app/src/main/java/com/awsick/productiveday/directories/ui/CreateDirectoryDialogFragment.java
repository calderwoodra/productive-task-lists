package com.awsick.productiveday.directories.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.awsick.productiveday.R;
import com.awsick.productiveday.directories.repo.DirectoryRepo;
import dagger.hilt.android.AndroidEntryPoint;
import javax.inject.Inject;

@AndroidEntryPoint
public final class CreateDirectoryDialogFragment extends DialogFragment {

  @Inject DirectoryRepo directoryRepo;

  private int currentDirectory;
  private EditText name;

  public static CreateDirectoryDialogFragment create(int currentDirectory) {
    CreateDirectoryDialogFragment dialog = new CreateDirectoryDialogFragment();
    dialog.currentDirectory = currentDirectory;
    return dialog;
  }

  @NonNull
  @Override
  public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
    return new AlertDialog.Builder(requireContext())
        .setTitle("Create directory")
        .setPositiveButton(
            "Create",
            (a, b) -> {
              EditText et = getDialog().findViewById(R.id.create_directory_name);
              directoryRepo.createDirectory(et.getText().toString(), currentDirectory);
            })
        .setNegativeButton("Cancel", (a, b) -> {})
        .setView(R.layout.dialog_fragment_create_directory)
        .setCancelable(true)
        .create();
  }
}

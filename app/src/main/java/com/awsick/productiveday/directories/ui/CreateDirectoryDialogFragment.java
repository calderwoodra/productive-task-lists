package com.awsick.productiveday.directories.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.awsick.productiveday.R;
import com.awsick.productiveday.directories.models.DirectoryReference;
import com.awsick.productiveday.directories.repo.DirectoryRepo;
import dagger.hilt.android.AndroidEntryPoint;
import javax.inject.Inject;

@AndroidEntryPoint
public final class CreateDirectoryDialogFragment extends DialogFragment {

  @Inject DirectoryRepo directoryRepo;

  private int currentDirectory;
  private DirectoryReference directoryToEdit;

  public static CreateDirectoryDialogFragment create(int currentDirectory) {
    CreateDirectoryDialogFragment dialog = new CreateDirectoryDialogFragment();
    dialog.currentDirectory = currentDirectory;
    return dialog;
  }

  public static CreateDirectoryDialogFragment create(
      int currentDirectory, DirectoryReference directoryToEdit) {
    CreateDirectoryDialogFragment dialog = new CreateDirectoryDialogFragment();
    dialog.currentDirectory = currentDirectory;
    dialog.directoryToEdit = directoryToEdit;
    return dialog;
  }

  @NonNull
  @Override
  public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
    boolean update = directoryToEdit != null;
    View view =
        LayoutInflater.from(requireActivity())
            .inflate(R.layout.dialog_fragment_create_directory, null);
    EditText et = view.findViewById(R.id.create_directory_name);
    if (directoryToEdit != null) {
      et.setText(directoryToEdit.name());
    }

    return new AlertDialog.Builder(requireContext())
        .setTitle(update ? "Update directory" : "Create directory")
        .setPositiveButton(
            update ? "Update" : "Create",
            (a, b) -> {
              if (update) {
                directoryRepo.updateDirectory(directoryToEdit, et.getText().toString());
              } else {
                directoryRepo.createDirectory(et.getText().toString(), currentDirectory);
              }
            })
        .setNegativeButton("Cancel", (a, b) -> {})
        .setView(view)
        .setCancelable(true)
        .create();
  }
}

package com.awsick.productiveday.common.dialogutils;

import android.content.Context;
import android.widget.ArrayAdapter;
import androidx.appcompat.app.AlertDialog;
import com.google.common.base.Strings;
import java.util.function.Consumer;

public class DialogUtils {

  public static void setErrorMessage(Context context, String message) {
    // TODO(allen): Add a support email
    setAlertMessage(context, "Looks like something went wrong.", message);
  }

  public static void setAlertMessage(Context context, String title, String message) {
    AlertDialog.Builder builder =
        new AlertDialog.Builder(context)
            .setTitle(title)
            .setCancelable(true)
            .setPositiveButton(
                "OK",
                (dialog, which) -> {
                  // Just dismiss.
                });

    if (!Strings.isNullOrEmpty(message)) {
      builder.setMessage(message);
    }

    builder.show();
  }

  public static AlertDialog.Builder createListDialog(
      Context context, Consumer<String> selection, String... items) {
    ArrayAdapter<String> arrayAdapter =
        new ArrayAdapter<>(context, android.R.layout.select_dialog_singlechoice);
    arrayAdapter.addAll(items);
    return new AlertDialog.Builder(context)
        .setAdapter(arrayAdapter, (dialog, which) -> selection.accept(arrayAdapter.getItem(which)));
  }
}

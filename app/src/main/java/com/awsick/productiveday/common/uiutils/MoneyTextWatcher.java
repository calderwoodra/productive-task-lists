package com.awsick.productiveday.common.uiutils;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import androidx.annotation.CallSuper;
import com.google.common.base.Strings;

public class MoneyTextWatcher implements TextWatcher {

  private final EditText editText;
  private final String current = "";

  public MoneyTextWatcher(EditText editText) {
    this.editText = editText;
  }

  @Override
  public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

  @Override
  public void onTextChanged(CharSequence s, int start, int before, int count) {}

  @Override
  @CallSuper
  public void afterTextChanged(Editable editable) {
    String s = editable.toString();
    if (Strings.isNullOrEmpty(s) || s.equals("$")) {
      setText(editable, "");
      return;
    }

    setText(editable, toDollarAmount(s));
  }

  public static String toDollarAmount(String text) {
    String cleanString = text.replaceAll("[$,]", "");
    int length = cleanString.length();
    if (cleanString.contains(".")) {
      length = cleanString.indexOf(".");
    }

    for (int i = length - 3; i > 0; i -= 3) {
      cleanString = cleanString.substring(0, i) + "," + cleanString.substring(i);
    }
    return "$" + cleanString;
  }

  private void setText(Editable editable, String formatted) {
    editText.removeTextChangedListener(this);
    editText.setText(formatted);
    editText.setSelection(formatted.length());

    editable.clear();
    editable.insert(0, formatted);
    editText.addTextChangedListener(this);
  }
}

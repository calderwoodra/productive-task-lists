package com.awsick.productiveday.common.uiutils;

import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import androidx.annotation.IdRes;

/**
 * Common utility for opening or closing the keyboard when an {@link EditText} gains or loses focus.
 */
public final class HandleKeyboardFocusChangeListener implements View.OnFocusChangeListener {

  @IdRes private final int viewId;
  private final InputMethodManager imm;

  public static void set(EditText editText) {
    editText.setOnFocusChangeListener(new HandleKeyboardFocusChangeListener(editText));
  }

  HandleKeyboardFocusChangeListener(EditText editText) {
    viewId = editText.getId();
    imm = editText.getContext().getSystemService(InputMethodManager.class);
  }

  @Override
  public void onFocusChange(View v, boolean hasFocus) {
    if (v.getId() == viewId) {
      if (hasFocus) {
        imm.showSoftInput(v, 0);
      } else {
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
      }
    }
  }
}

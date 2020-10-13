package com.awsick.productiveday.common.uiutils;

import android.view.View;
import android.view.inputmethod.InputMethodManager;

/** Utility specific to handling keyboard interactions. */
public final class KeyboardUtils {

  /** Hide the keyboard. */
  public static void hideKeyboardFrom(View view) {
    if (view != null) {
      InputMethodManager imm = view.getContext().getSystemService(InputMethodManager.class);
      imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
  }

  public static void openKeyboardFrom(View view) {
    if (view != null) {
      InputMethodManager imm = view.getContext().getSystemService(InputMethodManager.class);
      imm.showSoftInput(view, InputMethodManager.SHOW_FORCED);
    }
  }

  private KeyboardUtils() {}
}

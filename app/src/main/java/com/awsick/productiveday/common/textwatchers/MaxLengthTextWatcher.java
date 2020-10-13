package com.awsick.productiveday.common.textwatchers;

import android.widget.EditText;

/** Formats text input to be limited to a max length. */
public final class MaxLengthTextWatcher extends TextFormattingWatcher {

  private final int maxLength;

  public MaxLengthTextWatcher(EditText editText, int maxLength) {
    super(editText);
    this.maxLength = maxLength;
  }

  @Override
  protected String format(String input) {
    if (input.length() > maxLength) {
      return input.substring(0, maxLength);
    }
    return input;
  }
}

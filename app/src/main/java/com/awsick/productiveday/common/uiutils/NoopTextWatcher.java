package com.awsick.productiveday.common.uiutils;

import android.text.Editable;
import android.text.TextWatcher;

/** Simple {@link TextWatcher} with default implementations of each method. */
public class NoopTextWatcher implements TextWatcher {

  @Override
  public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

  @Override
  public void onTextChanged(CharSequence s, int start, int before, int count) {}

  @Override
  public void afterTextChanged(Editable s) {}
}

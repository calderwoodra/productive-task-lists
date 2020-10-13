package com.awsick.productiveday.common.textwatchers;

import android.telephony.PhoneNumberUtils;
import android.widget.EditText;

/** Formats text input for phone numbers. */
public final class PhoneNumberTextWatcher extends TextFormattingWatcher {

  public PhoneNumberTextWatcher(EditText editText) {
    super(editText);
  }

  @Override
  protected String format(String input) {
    String formatted = PhoneNumberUtils.formatNumber(input, "US");
    return formatted == null ? input : formatted;
  }
}

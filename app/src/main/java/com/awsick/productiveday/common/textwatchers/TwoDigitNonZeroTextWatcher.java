package com.awsick.productiveday.common.textwatchers;

import android.widget.EditText;
import com.awsick.productiveday.common.utils.StringUtils;

public final class TwoDigitNonZeroTextWatcher extends TextFormattingWatcher {

  public TwoDigitNonZeroTextWatcher(EditText editText) {
    super(editText);
  }

  @Override
  protected String format(String input) {
    input = StringUtils.numbersOnly(input);
    if (input.equals("0")) {
      return "1";
    }

    if (input.length() > 2) {
      return input.substring(0, 2);
    }
    return input;
  }
}

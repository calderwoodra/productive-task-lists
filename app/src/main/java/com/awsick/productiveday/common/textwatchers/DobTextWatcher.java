package com.awsick.productiveday.common.textwatchers;

import android.widget.EditText;
import com.awsick.productiveday.common.utils.StringUtils;

/** Formats text input for date of birth. */
public final class DobTextWatcher extends TextFormattingWatcher {

  public DobTextWatcher(EditText editText) {
    super(editText);
  }

  @Override
  protected String format(String input) {
    return formatted(input);
  }

  public static String formatted(String input) {
    String clean = StringUtils.numbersOnly(input);

    // 12
    if (clean.length() <= 2) {
      return clean;
    }
    // clean = 12/3....
    clean = StringUtils.insert(clean, "/", 2);

    // 12/30
    if (clean.length() <= 5) {
      return clean;
    }
    // clean = 12/30/1990
    clean = StringUtils.insert(clean, "/", 5);

    // trim extra characters
    if (clean.length() > 10) {
      return clean.substring(0, 10);
    }
    return clean;
  }
}

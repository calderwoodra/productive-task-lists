package com.awsick.productiveday.common.textwatchers;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import com.google.common.base.Strings;

/** Generic utility to facilitate easy text formatting. */
abstract class TextFormattingWatcher implements TextWatcher {

  protected final EditText editText;

  protected TextFormattingWatcher(EditText editText) {
    this.editText = editText;
  }

  @Override
  public final void beforeTextChanged(CharSequence s, int start, int count, int after) {}

  @Override
  public final void onTextChanged(CharSequence s, int start, int before, int count) {}

  @Override
  public final void afterTextChanged(Editable editable) {
    String s = editable.toString();
    if (hasNoUserInput(s)) {
      setText(editable, "");
      return;
    }
    setText(editable, format(s));
  }

  /**
   * Returns true if the user's input is empty.
   *
   * <p>For example, if the user is typing a dollar amount "$1", then deletes '1', the user's input
   * would be considered empty since all that remains is the '$' prefix.
   */
  protected static boolean hasNoUserInput(String s) {
    return Strings.isNullOrEmpty(s);
  }

  /** Returns a new formatted string converted from the text that's currently in the EditText. */
  protected abstract String format(String input);

  private void setText(Editable editable, String formatted) {
    editText.removeTextChangedListener(this);
    editText.setText(formatted);
    editText.setSelection(formatted.length());

    editable.clear();
    editable.insert(0, formatted);
    editText.addTextChangedListener(this);
  }
}

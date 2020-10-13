package com.awsick.productiveday.common.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewTreeObserver.OnPreDrawListener;
import com.awsick.productiveday.common.uiutils.KeyboardUtils;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.common.base.Strings;
import java.util.function.Consumer;

public class CenteredTextInputEditText extends TextInputEditText {

  private Integer paddingBottomBackup = null;

  // region Constructors
  public CenteredTextInputEditText(Context context) {
    super(context);
  }

  public CenteredTextInputEditText(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public CenteredTextInputEditText(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }
  // endregion

  // region LifeCycle
  @Override
  protected void onAttachedToWindow() {
    super.onAttachedToWindow();
    if (getOnFocusChangeListener() == null) {
      setOnFocusChangeListener((v, hasFocus) -> updateHintPosition(hasFocus));
    }

    getViewTreeObserver()
        .addOnPreDrawListener(
            new OnPreDrawListener() {
              @Override
              public boolean onPreDraw() {
                if (getHeight() > 0) {
                  getViewTreeObserver().removeOnPreDrawListener(this);
                  updateHintPosition(hasFocus());
                  return false;
                }
                return true;
              }
            });
  }
  // endregion

  // region Center hint
  private void updateHintPosition(boolean hasFocus) {
    boolean hasText = getText() != null && !Strings.isNullOrEmpty(getText().toString());
    if (paddingBottomBackup == null) {
      paddingBottomBackup = getPaddingBottom();
    }

    int bottomPadding = paddingBottomBackup;
    if (!hasFocus && !hasText) {
      bottomPadding += getTextInputTopSpace();
    }
    setPadding(getPaddingLeft(), getPaddingTop(), getPaddingRight(), bottomPadding);

    if (hasFocus) {
      KeyboardUtils.openKeyboardFrom(this);
    }
  }

  private int getTextInputTopSpace() {
    View currentView = this;
    int space = 0;
    do {
      space += currentView.getTop();
      currentView = (View) currentView.getParent();
    } while (!(currentView instanceof TextInputLayout));

    return space;
  }
  // endregion

  public void addAdditionalFocusListener(Consumer<Boolean> consumer) {
    setOnFocusChangeListener(
        new OnFocusChangeListener() {
          @Override
          public void onFocusChange(View v, boolean hasFocus) {
            consumer.accept(hasFocus);
            updateHintPosition(hasFocus);
          }
        });
  }
}

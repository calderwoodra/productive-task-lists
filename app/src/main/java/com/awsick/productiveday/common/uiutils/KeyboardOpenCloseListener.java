package com.awsick.productiveday.common.uiutils;

import android.app.Activity;
import android.util.Log;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import java.lang.ref.WeakReference;

/**
 * This class is a best effort at notifying listeners when they keyboard open/closes. Since not all
 * soft keyboards actually adjust the window size, this will not work in every situation. To use:
 *
 * <ul>
 *   <li>Add <code>android:windowSoftInputMode="adjustResize"</code> to the AndroidManifest
 *   <li>Add the observer in onCreate/OnResume/etc.
 *   <li>Remove the observer in onDestroy
 * </ul>
 */
public final class KeyboardOpenCloseListener implements OnGlobalLayoutListener {

  private static final String TAG = "KeyboardListener";

  private final WeakReference<ViewGroup> contentRoot;
  private final KeyboardOpenCloseCallback callback;
  private int screenHeight = 0;

  public KeyboardOpenCloseListener(Activity activity, KeyboardOpenCloseCallback callback) {
    contentRoot = new WeakReference<>(activity.findViewById(android.R.id.content));
    this.callback = callback;
  }

  @Override
  public void onGlobalLayout() {
    if (contentRoot.get() == null) {
      Log.e(TAG, "Did you forget to remove the keyboard observer in onDestroy?");
      return;
    }

    int newScreenHeight = contentRoot.get().getHeight();
    screenHeight = Math.max(screenHeight, contentRoot.get().getHeight());

    if (newScreenHeight < screenHeight * 0.9) {
      callback.onKeyboardOpened();
    } else {
      callback.onKeyboardClosed();
    }
  }

  public abstract static class KeyboardOpenCloseCallback {

    public void onKeyboardOpened() {}

    public void onKeyboardClosed() {}
  }
}

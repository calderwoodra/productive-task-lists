package com.awsick.productiveday.common.uiutils;

import android.content.Context;
import android.widget.Toast;
import androidx.annotation.CheckResult;
import java.util.concurrent.TimeUnit;

/**
 * Utility class for tracking back presses. This is useful if for some reason you want to consume
 * back presses but still allow the user to exit the current task after 2 clicks within a threshold.
 */
public final class OnBackPressedTwiceUtil {

  // If user presses back button twice in 3 seconds, close the app
  private static final long CLOSE_APP_THRESHOLD = TimeUnit.SECONDS.toNanos(3);

  private long timeSinceLastBackPress = -1;

  /** Return true if the user app should escape it's current activity. */
  @CheckResult
  public boolean userPressedBack(Context context) {
    long currentTime = System.nanoTime();
    long diff = Math.abs(currentTime - timeSinceLastBackPress);
    timeSinceLastBackPress = currentTime;
    if (diff > CLOSE_APP_THRESHOLD) {
      Toast.makeText(context, "Press back again to exit", Toast.LENGTH_SHORT).show();
      return false;
    }
    return true;
  }
}

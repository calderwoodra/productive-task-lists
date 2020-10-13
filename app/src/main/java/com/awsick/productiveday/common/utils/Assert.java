package com.awsick.productiveday.common.utils;

import android.os.Looper;
import androidx.annotation.Nullable;

/**
 * Utility for concisely validating expected behavior and killing the app when the unexpected
 * happens.
 */
public class Assert {

  private static boolean enableThreadAsserts = true;

  /** Throws if {@code expression} is false, else no-op. */
  public static void checkArgument(boolean expression) {
    checkArgument(expression, null);
  }

  /** Throws if {@code expression} is false, else no-op. */
  public static void checkArgument(
      boolean expression, @Nullable String messageTemplate, Object... args) {
    if (!expression) {
      throw new IllegalArgumentException(format(messageTemplate, args));
    }
  }

  /** Throws if {@code expression} is false, else no-op. */
  public static void checkState(boolean expression) {
    checkState(expression, null);
  }

  /** Throws if {@code expression} is false, else no-op. */
  public static void checkState(
      boolean expression, @Nullable String messageTemplate, Object... args) {
    if (!expression) {
      throw new IllegalStateException(format(messageTemplate, args));
    }
  }

  public static void setEnableThreadAsserts(boolean enableThreadAsserts) {
    Assert.enableThreadAsserts = enableThreadAsserts;
  }

  /** Throws if called OFF the main/UI thread, else no-op. */
  public static void isMainThread() {
    isMainThread(null);
  }

  /** Throws if called OFF the main/UI thread, else no-op. */
  public static void isMainThread(@Nullable String messageTemplate, Object... args) {
    if (!enableThreadAsserts) {
      return;
    }
    checkState(Looper.getMainLooper().equals(Looper.myLooper()), messageTemplate, args);
  }

  /** Throws if called ON the main/UI thread, else no-op. */
  public static void isWorkerThread() {
    isWorkerThread(null);
  }

  /** Throws if called ON the main/UI thread, else no-op. */
  public static void isWorkerThread(@Nullable String messageTemplate, Object... args) {
    if (!enableThreadAsserts) {
      return;
    }
    checkState(!Looper.getMainLooper().equals(Looper.myLooper()), messageTemplate, args);
  }

  private static String format(@Nullable String messageTemplate, Object... args) {
    if (messageTemplate == null) {
      return null;
    }
    return String.format(messageTemplate, args);
  }
}

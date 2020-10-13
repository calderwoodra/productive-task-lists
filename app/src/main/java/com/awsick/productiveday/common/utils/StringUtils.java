package com.awsick.productiveday.common.utils;

public final class StringUtils {

  /** Returns a string with only digits in it. */
  public static String numbersOnly(String s) {
    return s.replaceAll("[^\\d]", "");
  }

  /** Returns a new string with {@code insert} inserted into {@code base} at {@code index}. */
  public static String insert(String base, String insert, int index) {
    return base.substring(0, index) + insert + base.substring(index);
  }

  private StringUtils() {}
}

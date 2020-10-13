package com.awsick.productiveday.common.utils;

import com.google.common.base.Optional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * Generalized utilities for handling dates. It's not encouraged to add any utilities that are
 * specific to any given feature to this file.
 */
public final class DateUtils {

  // Different patterns to try and parse, based on whether the backend uses milliseconds or not.
  public static final String MS_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS";
  public static final String NO_MS_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";

  public static Optional<Date> convertStringToDate(String timeString, String format) {
    return Optional.fromNullable(convertStringToDateInternal(timeString, format));
  }

  public static Date convertStringToDate(String timeString) {
    if (timeString == null || timeString.isEmpty()) {
      return null;
    }

    // If the datestring doesn't include milliseconds, use the pattern without ms.
    Date parsedDate = convertStringToDateInternal(timeString, MS_DATE_FORMAT);
    if (parsedDate == null) {
      // Only log it to sentry if we have tried parsing both formats.
      parsedDate = convertStringToDateInternal(timeString, NO_MS_DATE_FORMAT);
    }
    return parsedDate;
  }

  /** Return a Date from a String parsed using GMT to match our server time. */
  private static Date convertStringToDateInternal(String timeString, String pattern) {
    try {
      SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.getDefault());
      sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
      return sdf.parse(timeString);
    } catch (ParseException e) {
      return null;
    }
  }

  /** Return a String from a Date formatted using GMT to match our server time. */
  public static String convertDateToString(Date date, String format) {
    SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());
    sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
    return sdf.format(date);
  }

  /** Return a String from a datestring formatted using GMT to match our server time. */
  public static String convertDateStringFormat(
      String dateString, String inputFormat, String outputFormat) {
    SimpleDateFormat sdfIn = new SimpleDateFormat(inputFormat, Locale.getDefault());
    SimpleDateFormat sdfOut = new SimpleDateFormat(outputFormat, Locale.getDefault());
    sdfOut.setTimeZone(TimeZone.getTimeZone("GMT"));
    Date date = null;
    try {
      date = sdfIn.parse(dateString);
    } catch (ParseException ignored) {
    }
    return sdfOut.format(date);
  }

  public static String getCurrentDateString() {
    return convertDateToString(new Date(getCurrentTimeMs()), MS_DATE_FORMAT);
  }

  public static String getCurrentMonthYearString() {
    SimpleDateFormat fmtOut = new SimpleDateFormat("MMMM yyyy", Locale.US);
    fmtOut.setTimeZone(TimeZone.getTimeZone("GMT"));
    Date now = new Date(getCurrentTimeMs());
    return fmtOut.format(now);
  }

  public static long getCurrentTimeMs() {
    return System.currentTimeMillis();
  }

  public static long getDaysDifferenceFromNow(Date date) {
    long diff = Math.abs((new Date()).getTime() - date.getTime());
    return TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
  }

  public static int getHoursDifferenceFromNow(Date date) {
    long diff = Math.abs((new Date()).getTime() - date.getTime());
    return (int) TimeUnit.MILLISECONDS.toHours(diff);
  }

  public static String getCenturiesDifferenceFromNow(Date date) {
    long days = getDaysDifferenceFromNow(date);
    return new BigDecimal(days).divide(new BigDecimal(365 * 100), 6, RoundingMode.UP).toString();
  }

  private DateUtils() {}
}
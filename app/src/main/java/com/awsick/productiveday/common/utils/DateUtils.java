package com.awsick.productiveday.common.utils;

import com.google.common.base.Optional;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Locale;

/**
 * Generalized utilities for handling dates. It's not encouraged to add any utilities that are
 * specific to any given feature to this file.
 */
public final class DateUtils {

  // Different patterns to try and parse, based on whether the backend uses milliseconds or not.
  public static final String HUMAN_TIME_FORMAT = "hh:mm aa";
  public static final String HUMAN_DATE_FORMAT = "EEE, MMM dd, yyyy";
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

  private static Date convertStringToDateInternal(String timeString, String pattern) {
    try {
      SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.getDefault());
      return sdf.parse(timeString);
    } catch (ParseException e) {
      return null;
    }
  }

  public static String convertDateToString(Date date, String format) {
    SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());
    return sdf.format(date);
  }

  public static String humanReadableTime(long millis) {
    return convertDateToString(new Date(millis), HUMAN_TIME_FORMAT);
  }

  public static String humanReadableDate(long millis) {
    return convertDateToString(new Date(millis), HUMAN_DATE_FORMAT);
  }

  public static long midnightTonightMillis(Clock clock) {
    return ZonedDateTime.now(clock)
        .withHour(23)
        .withMinute(59)
        .withSecond(0)
        .plusMinutes(1)
        .toInstant()
        .toEpochMilli();
  }

  private DateUtils() {}
}

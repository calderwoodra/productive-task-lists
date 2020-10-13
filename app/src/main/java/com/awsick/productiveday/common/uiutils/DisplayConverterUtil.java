package com.awsick.productiveday.common.uiutils;

import android.content.Context;
import android.util.DisplayMetrics;

/** Generalized utility for converting display units (px, dp, ect.) to other display units. */
public final class DisplayConverterUtil {

  public static int convertDpToPixel(float dp, Context context) {
    return (int)
        (dp
            * ((float) context.getResources().getDisplayMetrics().densityDpi
            / DisplayMetrics.DENSITY_DEFAULT));
  }

  private DisplayConverterUtil() {}
}

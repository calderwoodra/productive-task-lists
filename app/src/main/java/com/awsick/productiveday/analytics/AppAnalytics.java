package com.awsick.productiveday.analytics;

import android.util.Log;
import com.awsick.productiveday.BuildConfig;
import com.awsick.productiveday.application.PdApplication;
import java.util.HashMap;

public final class AppAnalytics {

  private static final String TAG = "AppAnalytics";

  private static final String PROP_APP_VERSION = "app_version";
  private static final String PROP_PLATFORM = "platform";
  private static final String VALUE_PLATFORM = "android";

  public static void track(String eventName) {
    track(eventName, new HashMap<String, Object>());
  }

  public static void track(String eventName, String key, Object value) {
    HashMap<String, Object> properties = new HashMap<>();
    properties.put(key, value);
    track(eventName, properties);
  }

  public static void track(String eventName, HashMap<String, Object> properties) {
    String propertiesString = "null";
    if (properties != null) {
      propertiesString = properties.toString();
    }
    Log.d(TAG, "Track: " + eventName + " Properties: " + propertiesString);
    properties.put(PROP_APP_VERSION, BuildConfig.VERSION_NAME);
    properties.put(PROP_PLATFORM, VALUE_PLATFORM);
    AppAnalyticsWorker.track(PdApplication.getContext(), eventName, properties);
  }

  /**
   * Helper to generate a hash map that contains the first {key: value} mapping passed.
   *
   * @param key   String
   * @param value String
   * @return HashMap {key: value}
   */
  public static HashMap<String, String> createStringProps(String key, String value) {
    HashMap<String, String> props = new HashMap<>();
    props.put(key, value);
    return props;
  }

  private AppAnalytics() {}
}

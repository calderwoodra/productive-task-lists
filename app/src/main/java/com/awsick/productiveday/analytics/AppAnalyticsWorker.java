package com.awsick.productiveday.analytics;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.core.util.Pair;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.OneTimeWorkRequest.Builder;
import androidx.work.Operation;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.awsick.productiveday.common.utils.Assert;
import com.awsick.productiveday.common.utils.DateUtils;
import com.google.common.base.Strings;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import retrofit2.Call;
import retrofit2.Response;

public final class AppAnalyticsWorker extends Worker {

  private static final String TAG = "AppAnalyticsWorker";
  private static final String ANALYTICS_EVENT_NAME_KEY = "analytics_event_name";
  private static final String ANALYTICS_EVENT_TIMESTAMP_KEY = "analytics_event_timestamp";

  /**
   * Schedule an analytics event to be logged.
   *
   * <p>Uses {@link WorkManager} to schedule a job using {@link #TAG} to label them and append them
   * to a queue using {@link ExistingWorkPolicy#APPEND}.
   *
   * <p>Tasks are executed:
   *
   * <ul>
   *   <li>In the order they're added (FIFO)
   *   <li>On a background thread managed by the OS
   *   <li>When the device is connected to the internet
   *   <li>Retried when there are failures with an exponential backoff
   * </ul>
   */
  public static Pair<OneTimeWorkRequest, Operation> track(
      Context context, String name, HashMap<String, Object> properties) {
    Constraints constraints =
        new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build();

    for (Entry<String, Object> entry : properties.entrySet()) {
      if (entry.getValue() instanceof List) {
        throw new IllegalStateException("Use arrays instead of lists");
      }
    }

    Data inputData =
        new Data.Builder()
            .putAll(properties)
            .putString(ANALYTICS_EVENT_NAME_KEY, name)
            .putString(
                ANALYTICS_EVENT_TIMESTAMP_KEY,
                DateUtils.convertDateToString(new Date(), DateUtils.NO_MS_DATE_FORMAT))
            .build();

    OneTimeWorkRequest request =
        new Builder(AppAnalyticsWorker.class)
            .setConstraints(constraints)
            .addTag(TAG)
            .setInputData(inputData)
            .build();
    Operation operation =
        WorkManager.getInstance(context).enqueueUniqueWork(TAG, ExistingWorkPolicy.APPEND, request);
    return Pair.create(request, operation);
  }

  public AppAnalyticsWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
    super(context, workerParams);
  }

  @NonNull
  @Override
  public Result doWork() {
    Log.d(TAG, "doWork");
    return logEvent();
  }

  private Result logEvent() {
    Assert.isWorkerThread();

    // Parse input
    HashMap<String, Object> properties = new HashMap<>(getInputData().getKeyValueMap());
    String eventName = (String) properties.remove(ANALYTICS_EVENT_NAME_KEY);
    String timestamp = (String) properties.remove(ANALYTICS_EVENT_TIMESTAMP_KEY);
    Assert.checkArgument(!Strings.isNullOrEmpty(eventName));
    Assert.checkArgument(!Strings.isNullOrEmpty(timestamp));

    // Build request
    // TODO(allen): Implement this call to amplitude
    // PostAnalyticsEventRequestBody body =
    //     new PostAnalyticsEventRequestBody(eventName, properties, timestamp);
    // AppBackendService.AppBackend backend =
    //     ServiceGenerator.createService(AppBackendService.AppBackend.class);
    Call<Void> call = null; // = backend.postAnalyticsEvent(body);

    // Execute the request in this thread
    Response<Void> response;
    try {
      response = call.execute();
    } catch (IOException e) {
      Log.e(TAG, "Flushing analytics event failed", e);
      return Result.retry();
    }

    // If the request failed, try again after a backoff
    if (response.code() != 200 && response.code() != 201) {
      Log.e(TAG, "Flushing analytics event request failed");
      return Result.retry();
    }

    // Event successfully logged
    Log.d(TAG, "Successfully flushed event. Response code: " + response.code());
    return Result.success();
  }
}

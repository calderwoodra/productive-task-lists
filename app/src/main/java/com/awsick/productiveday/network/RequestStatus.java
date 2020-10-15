package com.awsick.productiveday.network;

import android.widget.Toast;
import com.awsick.productiveday.BuildConfig;
import com.awsick.productiveday.application.PdApplication;
import com.awsick.productiveday.common.utils.Assert;
import com.google.common.base.Optional;

/** Helpful class for tracking the status of a request. */
public class RequestStatus<T> {

  public enum Status {
    INITIAL,
    PENDING,
    SUCCESS,
    FAILED,
  }

  public final Status status;
  private final Optional<T> result;
  private final Optional<Throwable> error;

  private RequestStatus(Status status, Optional<T> result, Optional<Throwable> error) {
    this.status = status;
    this.result = result;
    this.error = error;
  }

  public T getResult() {
    Assert.checkArgument(status == Status.SUCCESS, "No results available for status: " + status);
    return result.get();
  }

  public Optional<T> getFallbackResult() {
    Assert.checkArgument(status == Status.FAILED, "No results available for status: " + status);
    return result;
  }

  public Throwable getError() {
    Assert.checkArgument(status == Status.FAILED, "No error present for status: " + status);
    return error.get();
  }

  // Use sparingly, like when testing a new API or in a dev menu that doesn't have UI.
  public static <T> RequestStatus<T> successWithDebugToast(T result) {
    Toast.makeText(PdApplication.getContext(), "Success", Toast.LENGTH_SHORT).show();
    return success(result);
  }

  public static <T> RequestStatus<T> success(T result) {
    return new RequestStatus<>(Status.SUCCESS, Optional.of(result), Optional.absent());
  }

  // Use sparingly, like when testing a new API or in a dev menu that doesn't have UI.
  public static <T> RequestStatus<T> errorWithDebugToast(Throwable error) {
    if (BuildConfig.DEBUG) {
      Toast.makeText(
              PdApplication.getContext(), "failure: " + error.getMessage(), Toast.LENGTH_SHORT)
          .show();
    }
    return error(error);
  }

  public static <T> RequestStatus<T> error(Throwable error) {
    return new RequestStatus<>(Status.FAILED, Optional.absent(), Optional.of(error));
  }

  public static <T> RequestStatus<T> initial() {
    return new RequestStatus<>(Status.INITIAL, Optional.absent(), Optional.absent());
  }

  public static <T> RequestStatus<T> pending() {
    return new RequestStatus<>(Status.PENDING, Optional.absent(), Optional.absent());
  }

  public static <T> RequestStatus<T> errorWithFallback(T result, Throwable error) {
    return new RequestStatus<>(Status.FAILED, Optional.of(result), Optional.of(error));
  }
}

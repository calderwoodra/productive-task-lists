package com.awsick.productiveday.network.util;

import com.awsick.productiveday.network.RequestStatus;
import com.google.common.util.concurrent.FutureCallback;
import java.util.function.Consumer;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

/** Helpful utilities to reduce boilerplate with handling {@link RequestStatus}. */
public class RequestStatusUtils<T> {

  public static <T, R> RequestStatus<R> identity(
      RequestStatus<T> requestStatus, SuccessCallback<T, R> callback) {
    switch (requestStatus.status) {
      case INITIAL:
        return RequestStatus.initial();
      case PENDING:
        return RequestStatus.pending();
      case SUCCESS:
        return RequestStatus.success(callback.onSuccess(requestStatus.getResult()));
      case FAILED:
        return RequestStatus.error(requestStatus.getError());
    }
    throw new IllegalStateException("Unhandled status: " + requestStatus.status);
  }

  public interface SuccessCallback<T, R> {

    R onSuccess(T result);
  }

  public static boolean areAllSuccess(RequestStatus<?>... statuses) {
    for (RequestStatus<?> status : statuses) {
      if (status.status != RequestStatus.Status.SUCCESS) {
        return false;
      }
    }
    return true;
  }

  public static boolean anyFailures(RequestStatus<?>... statuses) {
    for (RequestStatus<?> status : statuses) {
      if (status.status == RequestStatus.Status.FAILED) {
        return true;
      }
    }
    return false;
  }

  public static <V> FutureCallback<V> futureCallback(
      RequestStatusLiveData<?> liveData, Consumer<V> onSuccess) {
    return new FutureCallback<V>() {
      @Override
      public void onSuccess(@NullableDecl V result) {
        onSuccess.accept(result);
      }

      @Override
      public void onFailure(Throwable throwable) {
        liveData.setValue(RequestStatus.error(throwable));
      }
    };
  }

  private RequestStatusUtils() {}
}

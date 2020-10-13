package com.awsick.productiveday.network.util;

import com.awsick.productiveday.network.RequestStatus;

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

  private RequestStatusUtils() {}
}

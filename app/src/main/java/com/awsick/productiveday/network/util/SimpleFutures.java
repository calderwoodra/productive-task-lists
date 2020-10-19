package com.awsick.productiveday.network.util;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.concurrent.Executor;

public final class SimpleFutures {

  public static <V> void addCallback(
      ListenableFuture<V> future, SimpleFutureCallback<V> callback, Executor executor) {
    Futures.addCallback(future, callback, executor);
  }

  private SimpleFutures() {}
}

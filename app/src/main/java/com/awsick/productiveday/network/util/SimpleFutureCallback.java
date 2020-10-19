package com.awsick.productiveday.network.util;

import androidx.annotation.NonNull;
import com.google.common.util.concurrent.FutureCallback;

public interface SimpleFutureCallback<V> extends FutureCallback<V> {

  @Override
  default void onFailure(@NonNull Throwable throwable) {
    throw new RuntimeException(throwable);
  }
}

package com.awsick.productiveday.network.util;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import com.awsick.productiveday.network.RequestStatus;
import java.util.Objects;

public class RequestStatusLiveData<T> extends MutableLiveData<RequestStatus<T>> {

  public RequestStatusLiveData() {
    setValue(RequestStatus.initial());
  }

  @Override
  public void setValue(@NonNull RequestStatus<T> value) {
    super.setValue(Objects.requireNonNull(value));
  }

  @NonNull
  @Override
  public RequestStatus<T> getValue() {
    return Objects.requireNonNull(super.getValue());
  }
}

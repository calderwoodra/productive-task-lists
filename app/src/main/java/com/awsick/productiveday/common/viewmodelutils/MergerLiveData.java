package com.awsick.productiveday.common.viewmodelutils;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import com.google.common.base.Optional;
import com.google.errorprone.annotations.DoNotCall;

/** {@link LiveData} for merging up to 3 sources together and transforming them. */
public abstract class MergerLiveData<T, U, V, W> extends MediatorLiveData<T> {

  private final LiveData<U> source1;
  private final LiveData<V> source2;
  private final LiveData<W> source3;

  public MergerLiveData(LiveData<U> source1) {
    this(Optional.of(source1), Optional.absent(), Optional.absent());
  }

  public MergerLiveData(LiveData<U> source1, LiveData<V> source2) {
    this(Optional.of(source1), Optional.of(source2), Optional.absent());
  }

  public MergerLiveData(LiveData<U> source1, LiveData<V> source2, LiveData<W> source3) {
    this(Optional.of(source1), Optional.of(source2), Optional.of(source3));
  }

  private MergerLiveData(
      Optional<LiveData<U>> source1, Optional<LiveData<V>> source2, Optional<LiveData<W>> source3) {
    this.source1 = source1.orNull();
    this.source2 = source2.orNull();
    this.source3 = source3.orNull();
    setValueInternal(onChanged());

    if (this.source1 != null) {
      addSource(this.source1, data -> setValueInternal(onChanged()));
    }

    if (this.source2 != null) {
      addSource(this.source2, data -> setValueInternal(onChanged()));
    }

    if (this.source3 != null) {
      addSource(this.source3, data -> setValueInternal(onChanged()));
    }
  }

  public final U getSource1() {
    return source1.getValue();
  }

  public final V getSource2() {
    return source2.getValue();
  }

  public final W getSource3() {
    return source3.getValue();
  }

  /** Called when any of the given LiveData updates. */
  protected abstract T onChanged();

  @DoNotCall("Return the value in onChanged instead.")
  @Override
  public final void setValue(T value) {
    throw new UnsupportedOperationException("Return the designated value in onChanged");
  }

  private void setValueInternal(T value) {
    if (getValue() == null || allowDuplicateValues() || !areEqual(getValue(), value)) {
      super.setValue(value);
    }
  }

  /** Returns true if {@code val1} and {@code val2} are equal. Otherwise, false. */
  public abstract boolean areEqual(T val1, T val2);

  /** Returns true if it's okay {@link #setValue(Object)} to post the same value. */
  protected static boolean allowDuplicateValues() {
    return false;
  }
}

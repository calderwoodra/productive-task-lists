package com.awsick.productiveday.common.viewmodelutils;

import android.util.Log;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A lifecycle-aware observable that only sends updates once. This is especially useful for events
 * like navigation and SnackBar messages.
 *
 * <p>This avoids a common problem with other instances of LiveData: on a configuration change (like
 * leaving and returning to a view/fragment/activity), the lifecycle-owner will re-observe an old
 * event which may trigger an unintended action. This LiveData only calls the observable if there's
 * an explicit call to setValue().
 *
 * <p>Note: Only one observer is allowed at a time.
 */
public class SingleLiveEvent<T> extends MutableLiveData<T> {

  // Fragments sent to the backstack do not trigger onViewDestroyed in tests, so disable our assert.
  @VisibleForTesting public static boolean disableObserversForTesting = false;

  private final AtomicBoolean pending = new AtomicBoolean(false);

  @Override
  @MainThread
  public void observe(@NonNull LifecycleOwner owner, @NonNull Observer<? super T> observer) {
    if (hasActiveObservers() && !disableObserversForTesting) {
      Log.e("SingleLiveEvent", "Only one active observer is allowed at a time.");
    }
    super.observe(
        owner,
        t -> {
          if (pending.compareAndSet(true, false)) {
            observer.onChanged(t);
          }
        });
  }

  @Override
  @MainThread
  public void setValue(@Nullable T t) {
    pending.set(true);
    super.setValue(t);
  }
}

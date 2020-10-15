package com.awsick.productiveday.common.uiutils;

import androidx.annotation.CheckResult;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import com.google.common.base.Optional;

/** Utility methods for working with Fragments. */
public class FragmentUtils {

  private static Object parentForTesting;

  @VisibleForTesting(otherwise = VisibleForTesting.NONE)
  public static void setParentForTesting(Object parentForTesting) {
    FragmentUtils.parentForTesting = parentForTesting;
  }

  /**
   * Return an instance of the the callback class if possible, otherwise {@link Optional#absent()}
   *
   * <p>Implementation detail: Checks all possible parents, such as, parent fragments and
   * activities.
   */
  public static <T> Optional<T> getParent(
      @NonNull Fragment fragment, @NonNull Class<T> callbackInterface) {
    if (callbackInterface.isInstance(parentForTesting)) {
      @SuppressWarnings("unchecked") // Casts are checked using runtime methods
      T parent = (T) parentForTesting;
      return Optional.of(parent);
    }
    Fragment parentFragment = fragment.getParentFragment();
    if (parentFragment instanceof NavHostFragment) {
      return getParent(parentFragment, callbackInterface);
    }

    if (parentFragment instanceof FragmentUtilListener) {
      T parent = ((FragmentUtilListener) parentFragment).getImpl(callbackInterface);
      if (parent != null) {
        return Optional.of(parent);
      }
    }

    if (parentFragment != null) {
      return getParent(parentFragment, callbackInterface);
    }

    if (fragment.getActivity() instanceof FragmentUtilListener) {
      T parent = ((FragmentUtilListener) fragment.getActivity()).getImpl(callbackInterface);
      if (parent != null) {
        return Optional.of(parent);
      }
    }

    // None of the parents implement the given interface
    return Optional.absent();
  }

  /** Returns the parent or throws. Should perform check elsewhere(e.g. onAttach, newInstance). */
  @NonNull
  @CheckResult(suggest = "#checkParent(Fragment, Class)}")
  public static <T> T getParentUnsafe(
      @NonNull Fragment fragment, @NonNull Class<T> callbackInterface) {
    return getParent(fragment, callbackInterface).get();
  }

  /**
   * Ensures fragment's parent implements {@code callbackInterface}.
   *
   * @throws IllegalStateException if no parents are found that implement callbackInterface
   */
  public static void checkParent(@NonNull Fragment frag, @NonNull Class<?> callbackInterface)
      throws IllegalStateException {
    if (parentForTesting != null) {
      return;
    }
    if (!FragmentUtils.getParent(frag, callbackInterface).isPresent()) {
      String parent =
          frag.getParentFragment() == null
              ? frag.getActivity().getClass().getName()
              : frag.getParentFragment().getClass().getName();
      throw new IllegalStateException(
          frag.getClass().getName()
              + " must be added to a parent"
              + " that implements "
              + callbackInterface.getName()
              + ". Instead found "
              + parent);
    }
  }

  /** Interface for activities to implement to avoid implementing arbitrary listeners. */
  public interface FragmentUtilListener {

    /** Returns an implementation of T if parent has one, otherwise null. */
    @Nullable
    <T> T getImpl(Class<T> callbackInterface);
  }
}

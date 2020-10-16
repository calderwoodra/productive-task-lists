package com.awsick.productiveday.common.utils;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.function.Function;
import java.util.stream.Collector;

public final class ImmutableUtils {

  private static final Collector<Object, ?, ImmutableList<Object>> TO_IMMUTABLE_LIST =
      Collector.of(
          ImmutableList::builder,
          ImmutableList.Builder::add,
          (objectBuilder, objectBuilder2) -> objectBuilder.addAll(objectBuilder2.build()),
          ImmutableList.Builder::build);

  public static <E> Collector<E, ?, ImmutableList<E>> toImmutableList() {
    return (Collector) TO_IMMUTABLE_LIST;
  }

  public static <T, K, V> Collector<T, ?, ImmutableMap<K, V>> toImmutableMap(
      Function<? super T, ? extends K> keyFunction,
      Function<? super T, ? extends V> valueFunction) {
    checkNotNull(keyFunction);
    checkNotNull(valueFunction);
    return Collector.of(
        ImmutableMap.Builder<K, V>::new,
        (builder, input) -> builder.put(keyFunction.apply(input), valueFunction.apply(input)),
        (kvBuilder, kvBuilder2) -> kvBuilder.putAll(kvBuilder2.build()),
        ImmutableMap.Builder::build);
  }

  private ImmutableUtils() {}
}

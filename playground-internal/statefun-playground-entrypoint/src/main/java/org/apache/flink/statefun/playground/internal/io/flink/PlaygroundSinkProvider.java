package org.apache.flink.statefun.playground.internal.io.flink;

import org.apache.flink.statefun.flink.io.spi.SinkProvider;
import org.apache.flink.statefun.playground.internal.io.spec.PlaygroundEgressSpec;
import org.apache.flink.statefun.sdk.io.EgressSpec;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;

public class PlaygroundSinkProvider implements SinkProvider {
  @Override
  public <T> SinkFunction<T> forSpec(EgressSpec<T> spec) {
    final PlaygroundEgressSpec egressSpec = asPlaygroundEgressSpec(spec);
    return new PlaygroundEgress<>(egressSpec.getPort(), egressSpec.getTopics());
  }

  private static <T> PlaygroundEgressSpec asPlaygroundEgressSpec(EgressSpec<T> spec) {
    if (spec instanceof PlaygroundEgressSpec) {
      return (PlaygroundEgressSpec) spec;
    }

    throw new IllegalArgumentException(String.format("Unknown egress spec %s.", spec));
  }
}

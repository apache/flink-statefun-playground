package org.apache.flink.statefun.playground.internal.io.flink;

import org.apache.flink.statefun.flink.io.spi.SourceProvider;
import org.apache.flink.statefun.playground.internal.io.spec.PlaygroundIngressSpec;
import org.apache.flink.statefun.sdk.io.IngressSpec;
import org.apache.flink.streaming.api.functions.source.SourceFunction;

public class PlaygroundSourceProvider implements SourceProvider {
  @Override
  public <T> SourceFunction<T> forSpec(IngressSpec<T> spec) {
    final PlaygroundIngressSpec ingressSpec = asPlaygroundIngressSpec(spec);

    return new PlaygroundIngress<T>(ingressSpec.getPort());
  }

  private static <T> PlaygroundIngressSpec asPlaygroundIngressSpec(IngressSpec<T> spec) {
    if (spec instanceof PlaygroundIngressSpec) {
      return (PlaygroundIngressSpec) spec;
    }

    throw new IllegalArgumentException(String.format("Unknown ingress spec %s", spec));
  }
}

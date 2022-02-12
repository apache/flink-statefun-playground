package org.apache.flink.statefun.playground.internal.io.binders.egress.v1;

import org.apache.flink.statefun.extensions.ComponentBinder;
import org.apache.flink.statefun.extensions.ComponentJsonObject;
import org.apache.flink.statefun.playground.internal.io.binders.Utils;
import org.apache.flink.statefun.playground.internal.io.spec.PlaygroundEgressSpec;
import org.apache.flink.statefun.sdk.TypeName;
import org.apache.flink.statefun.sdk.spi.StatefulFunctionModule;

public class PlaygroundEgressBinder implements ComponentBinder {

  static final PlaygroundEgressBinder INSTANCE = new PlaygroundEgressBinder();
  static final TypeName KIND_TYPE = TypeName.parseFrom("io.statefun.playground.v1/egress");

  @Override
  public void bind(
      ComponentJsonObject component, StatefulFunctionModule.Binder remoteModuleBinder) {
    Utils.validateComponent(component, KIND_TYPE);
    final PlaygroundEgressSpec playgroundEgressSpec =
        Utils.parseJson(component.specJsonNode(), PlaygroundEgressSpec.class);

    remoteModuleBinder.bindEgress(playgroundEgressSpec);
  }
}

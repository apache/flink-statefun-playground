package org.apache.flink.statefun.playground.internal.io.binders.ingress.v1;

import org.apache.flink.statefun.extensions.ComponentBinder;
import org.apache.flink.statefun.extensions.ComponentJsonObject;
import org.apache.flink.statefun.flink.io.common.AutoRoutableProtobufRouter;
import org.apache.flink.statefun.playground.internal.io.binders.Utils;
import org.apache.flink.statefun.playground.internal.io.spec.PlaygroundIngressSpec;
import org.apache.flink.statefun.sdk.TypeName;
import org.apache.flink.statefun.sdk.spi.StatefulFunctionModule;

public class PlaygroundIngressBinder implements ComponentBinder {

  static final PlaygroundIngressBinder INSTANCE = new PlaygroundIngressBinder();
  static final TypeName KIND_TYPE = TypeName.parseFrom("io.statefun.playground.v1/ingress");

  @Override
  public void bind(
      ComponentJsonObject component, StatefulFunctionModule.Binder remoteModuleBinder) {
    Utils.validateComponent(component, KIND_TYPE);
    final PlaygroundIngressSpec playgroundIngressSpec =
        Utils.parseJson(component.specJsonNode(), PlaygroundIngressSpec.class);

    remoteModuleBinder.bindIngress(playgroundIngressSpec);
    remoteModuleBinder.bindIngressRouter(
        playgroundIngressSpec.id(), new AutoRoutableProtobufRouter());
  }
}

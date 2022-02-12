package org.apache.flink.statefun.playground.internal.io.spec;

import com.google.protobuf.Message;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonCreator;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import org.apache.flink.statefun.playground.internal.io.Constants;
import org.apache.flink.statefun.sdk.IngressType;
import org.apache.flink.statefun.sdk.io.IngressIdentifier;
import org.apache.flink.statefun.sdk.io.IngressSpec;

@JsonDeserialize(builder = PlaygroundIngressSpec.Builder.class)
public class PlaygroundIngressSpec implements IngressSpec<Message> {

  private final int port;

  private PlaygroundIngressSpec(int port) {
    this.port = port;
  }

  public int getPort() {
    return port;
  }

  @Override
  public IngressIdentifier<Message> id() {
    return Constants.INGRESS_IDENTIFIER;
  }

  @Override
  public IngressType type() {
    return Constants.INGRESS_TYPE;
  }

  @JsonPOJOBuilder
  public static final class Builder {
    private final int port;

    @JsonCreator
    private Builder(@JsonProperty("port") int port) {
      this.port = port;
    }

    public PlaygroundIngressSpec build() {
      return new PlaygroundIngressSpec(port);
    }
  }
}

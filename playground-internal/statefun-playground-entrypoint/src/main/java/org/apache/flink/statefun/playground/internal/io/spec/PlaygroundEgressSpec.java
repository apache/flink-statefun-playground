package org.apache.flink.statefun.playground.internal.io.spec;

import java.util.Collections;
import java.util.Set;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonCreator;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import org.apache.flink.statefun.playground.internal.io.Constants;
import org.apache.flink.statefun.sdk.EgressType;
import org.apache.flink.statefun.sdk.io.EgressIdentifier;
import org.apache.flink.statefun.sdk.io.EgressSpec;
import org.apache.flink.statefun.sdk.reqreply.generated.TypedValue;

@JsonDeserialize(builder = PlaygroundEgressSpec.Builder.class)
public class PlaygroundEgressSpec implements EgressSpec<TypedValue> {

  private final int port;
  private final Set<String> topics;

  private PlaygroundEgressSpec(int port, Set<String> topics) {
    this.port = port;
    this.topics = topics;
  }

  @Override
  public EgressIdentifier<TypedValue> id() {
    return Constants.EGRESS_IDENTIFIER;
  }

  @Override
  public EgressType type() {
    return Constants.EGRESS_TYPE;
  }

  public int getPort() {
    return port;
  }

  public Set<String> getTopics() {
    return Collections.unmodifiableSet(topics);
  }

  @JsonPOJOBuilder
  public static final class Builder {
    private final int port;
    private final Set<String> topics;

    @JsonCreator
    private Builder(@JsonProperty("port") int port, @JsonProperty("topics") Set<String> topics) {
      this.port = port;
      this.topics = topics;
    }

    public PlaygroundEgressSpec build() {
      return new PlaygroundEgressSpec(port, topics);
    }
  }
}

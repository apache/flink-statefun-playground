package org.apache.flink.statefun.playground.internal.io.flink;

import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;

public class EgressRecord {
  @JsonProperty("topic")
  private String topic;

  @JsonProperty("payload")
  private String payload;

  public EgressRecord() {}

  public String getTopic() {
    return topic;
  }

  public String getPayload() {
    return payload;
  }
}

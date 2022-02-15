package org.apache.flink.statefun.playground.java.connectedcomponents.types;

import com.fasterxml.jackson.annotation.JsonProperty;

public class EgressRecord {
  @JsonProperty("topic")
  private String topic;

  @JsonProperty("payload")
  private String payload;

  public EgressRecord() {
    this(null, null);
  }

  public EgressRecord(String topic, String payload) {
    this.topic = topic;
    this.payload = payload;
  }

  public String getTopic() {
    return topic;
  }

  public String getPayload() {
    return payload;
  }
}

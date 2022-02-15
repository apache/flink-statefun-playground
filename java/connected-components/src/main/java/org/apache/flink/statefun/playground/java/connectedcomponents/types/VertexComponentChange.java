package org.apache.flink.statefun.playground.java.connectedcomponents.types;

import com.fasterxml.jackson.annotation.JsonProperty;

public class VertexComponentChange {

  @JsonProperty("source")
  private int source;

  @JsonProperty("target")
  private int target;

  @JsonProperty("component_id")
  private int componentId;

  public VertexComponentChange() {
    this(0, 0, 0);
  }

  private VertexComponentChange(int source, int target, int componentId) {
    this.source = source;
    this.target = target;
    this.componentId = componentId;
  }

  public int getSource() {
    return source;
  }

  public int getTarget() {
    return target;
  }

  public int getComponentId() {
    return componentId;
  }

  public static VertexComponentChange create(int source, int target, int componentId) {
    return new VertexComponentChange(source, target, componentId);
  }
}

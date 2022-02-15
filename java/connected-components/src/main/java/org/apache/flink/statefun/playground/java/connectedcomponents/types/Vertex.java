package org.apache.flink.statefun.playground.java.connectedcomponents.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class Vertex {

  @JsonProperty("vertex_id")
  private int vertexId;

  @JsonProperty("neighbours")
  private List<Integer> neighbours;

  public Vertex() {}

  public int getVertexId() {
    return vertexId;
  }

  public List<Integer> getNeighbours() {
    return neighbours;
  }
}

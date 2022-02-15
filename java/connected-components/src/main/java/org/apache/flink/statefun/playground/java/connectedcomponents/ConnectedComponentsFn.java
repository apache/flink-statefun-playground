package org.apache.flink.statefun.playground.java.connectedcomponents;

import static org.apache.flink.statefun.playground.java.connectedcomponents.types.Types.EGRESS_RECORD_JSON_TYPE;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import org.apache.flink.statefun.playground.java.connectedcomponents.types.EgressRecord;
import org.apache.flink.statefun.playground.java.connectedcomponents.types.Types;
import org.apache.flink.statefun.playground.java.connectedcomponents.types.Vertex;
import org.apache.flink.statefun.playground.java.connectedcomponents.types.VertexComponentChange;
import org.apache.flink.statefun.sdk.java.Context;
import org.apache.flink.statefun.sdk.java.StatefulFunction;
import org.apache.flink.statefun.sdk.java.StatefulFunctionSpec;
import org.apache.flink.statefun.sdk.java.TypeName;
import org.apache.flink.statefun.sdk.java.ValueSpec;
import org.apache.flink.statefun.sdk.java.message.EgressMessageBuilder;
import org.apache.flink.statefun.sdk.java.message.Message;
import org.apache.flink.statefun.sdk.java.message.MessageBuilder;

/**
 * A stateful function that computes the connected component for a stream of vertices.
 *
 * <p>The underlying algorithm is a form of label propagation and works by recording for every
 * vertex its component id. Whenever a vertex is created or its component id changes, it will send
 * this update to all of its neighbours. Every neighbour will compare the broadcast component id
 * with its own id. If the id is lower than its own, then it will accept this component id and
 * broadcast this change to its neighbours. If the own component id is smaller, then it answers to
 * the broadcaster by sending its own component id.
 *
 * <p>That way, the minimum component id of each connected component will be broadcast throughout
 * the whole connected component. Eventually, every vertex will have heard of the minimum component
 * id and have accepted it.
 *
 * <p>Every component id change will be output to the {@link #PLAYGROUND_EGRESS} as a connected
 * component change.
 *
 * @see <a href="https://en.wikipedia.org/wiki/Label_propagation_algorithm">Label propagation
 *     algorithm</a>
 */
final class ConnectedComponentsFn implements StatefulFunction {

  /** The current component id of a vertex. */
  private static final ValueSpec<Integer> COMPONENT_ID =
      ValueSpec.named("componentId").withIntType();

  /** List of known neighbours of a vertex. */
  private static final ValueSpec<Set<Integer>> NEIGHBOURS_VALUE =
      ValueSpec.named("neighbours").withCustomType(Types.NEIGHBOURS_TYPE);

  static final TypeName TYPE_NAME = TypeName.typeNameOf("connected-components.fns", "vertex");
  static final StatefulFunctionSpec SPEC =
      StatefulFunctionSpec.builder(TYPE_NAME)
          .withSupplier(ConnectedComponentsFn::new)
          .withValueSpecs(COMPONENT_ID, NEIGHBOURS_VALUE)
          .build();

  static final TypeName PLAYGROUND_EGRESS = TypeName.typeNameOf("io.statefun.playground", "egress");

  @Override
  public CompletableFuture<Void> apply(Context context, Message message) {
    // initialize a new vertex
    if (message.is(Types.VERTEX_INIT_TYPE)) {
      final Vertex vertex = message.as(Types.VERTEX_INIT_TYPE);

      int currentComponentId = context.storage().get(COMPONENT_ID).orElse(Integer.MAX_VALUE);
      final Set<Integer> currentNeighbours = getCurrentNeighbours(context);

      if (currentComponentId > vertex.getVertexId()) {
        updateComponentId(context, vertex.getVertexId(), vertex.getVertexId());
        currentComponentId = vertex.getVertexId();
      }

      final HashSet<Integer> neighbourDiff = new HashSet<>(vertex.getNeighbours());
      neighbourDiff.removeAll(currentNeighbours);

      broadcastVertexConnectedComponentChange(
          context, vertex.getVertexId(), neighbourDiff, currentComponentId);

      // update the neighbours
      neighbourDiff.addAll(currentNeighbours);
      context.storage().set(NEIGHBOURS_VALUE, neighbourDiff);
    }
    // a neighbours component id has changed
    else if (message.is(Types.VERTEX_COMPONENT_CHANGE_TYPE)) {
      final VertexComponentChange vertexComponentChange =
          message.as(Types.VERTEX_COMPONENT_CHANGE_TYPE);
      final Set<Integer> currentNeighbours = getCurrentNeighbours(context);

      // only process the message if we can reach the source --> connected components with directed
      // edges
      if (currentNeighbours.contains(vertexComponentChange.getSource())) {
        final int componentIdCandidate = vertexComponentChange.getComponentId();
        final int currentComponentId =
            context.storage().get(COMPONENT_ID).orElse(Integer.MAX_VALUE);

        if (currentComponentId < componentIdCandidate) {
          sendVertexConnectedComponentChange(
              context,
              vertexComponentChange.getTarget(),
              vertexComponentChange.getSource(),
              currentComponentId);
        } else if (currentComponentId > componentIdCandidate) {
          updateComponentId(context, vertexComponentChange.getTarget(), componentIdCandidate);
          currentNeighbours.remove(vertexComponentChange.getSource());
          broadcastVertexConnectedComponentChange(
              context, vertexComponentChange.getTarget(), currentNeighbours, componentIdCandidate);
        }
      }
    }

    return context.done();
  }

  private Set<Integer> getCurrentNeighbours(Context context) {
    return context.storage().get(NEIGHBOURS_VALUE).orElse(Collections.emptySet());
  }

  private void broadcastVertexConnectedComponentChange(
      Context context, int source, Iterable<Integer> neighbours, int componentId) {
    for (Integer neighbour : neighbours) {
      sendVertexConnectedComponentChange(context, source, neighbour, componentId);
    }
  }

  private void sendVertexConnectedComponentChange(
      Context context, int source, int target, int currentComponentId) {
    final VertexComponentChange vertexComponentChange =
        VertexComponentChange.create(source, target, currentComponentId);
    context.send(
        MessageBuilder.forAddress(TYPE_NAME, String.valueOf(target))
            .withCustomType(Types.VERTEX_COMPONENT_CHANGE_TYPE, vertexComponentChange)
            .build());
  }

  private void updateComponentId(Context context, int vertexId, int componentId) {
    context.storage().set(COMPONENT_ID, componentId);
    outputConnectedComponentChange(context, vertexId, componentId);
  }

  private void outputConnectedComponentChange(Context context, int vertexId, int componentId) {
    context.send(
        EgressMessageBuilder.forEgress(PLAYGROUND_EGRESS)
            .withCustomType(
                EGRESS_RECORD_JSON_TYPE,
                new EgressRecord(
                    "connected-component-changes",
                    String.format("Vertex %s belongs to component %s.", vertexId, componentId)))
            .build());
  }
}

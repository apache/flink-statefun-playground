package org.apache.flink.statefun.playground.internal.io.flink;

import com.google.protobuf.ByteString;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.util.StatusCodes;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.flink.util.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class EgressWebServer implements AutoCloseable {
  private static final Logger LOG = LoggerFactory.getLogger(EgressWebServer.class);

  private final ConcurrentMap<String, BlockingQueue<ByteString>> queues;
  private final Undertow server;

  EgressWebServer(int port, Set<String> topics) {
    this.queues = createQueues(topics);
    this.server =
        Undertow.builder()
            .addHttpListener(port, "0.0.0.0")
            .setHandler(new EgressHttpHandler(queues))
            .build();

    server.start();
  }

  private static ConcurrentMap<String, BlockingQueue<ByteString>> createQueues(Set<String> topics) {
    return topics.stream()
        .collect(
            Collectors.toConcurrentMap(
                Function.identity(), ignored -> new ArrayBlockingQueue<>(1 << 20)));
  }

  @Override
  public void close() {
    server.stop();
  }

  public void offer(String topic, ByteString message) {
    if (!Preconditions.checkNotNull(queues.get(topic)).offer(message)) {
      LOG.info(
          "Dropping message {} for topic {} because the queue is currently full.", message, topic);
    }
  }

  private static final class EgressHttpHandler implements HttpHandler {
    private final Map<String, BlockingQueue<ByteString>> queues;

    private EgressHttpHandler(Map<String, BlockingQueue<ByteString>> queues) {
      this.queues = queues;
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) {
      final String topic = exchange.getRelativePath().substring(1);

      final BlockingQueue<ByteString> queue = queues.get(topic);

      if (queue != null) {
        final ByteString message = queue.poll();

        if (message != null) {
          exchange.getResponseHeaders().put(Headers.STATUS, StatusCodes.OK);
          exchange.getResponseSender().send(message.asReadOnlyByteBuffer());
        } else {
          exchange.getResponseHeaders().put(Headers.STATUS, StatusCodes.NOT_FOUND);
        }
      } else {
        exchange.getResponseHeaders().put(Headers.STATUS, StatusCodes.METHOD_NOT_ALLOWED);
      }

      exchange.endExchange();
    }
  }
}

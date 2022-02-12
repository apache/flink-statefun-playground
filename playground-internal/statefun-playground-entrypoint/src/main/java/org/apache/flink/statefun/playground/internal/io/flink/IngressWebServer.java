package org.apache.flink.statefun.playground.internal.io.flink;

import com.google.protobuf.ByteString;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HeaderMap;
import io.undertow.util.HeaderValues;
import io.undertow.util.Headers;
import io.undertow.util.StatusCodes;
import java.util.Collections;
import java.util.concurrent.BlockingQueue;
import org.apache.flink.statefun.flink.io.generated.AutoRoutable;
import org.apache.flink.statefun.flink.io.generated.RoutingConfig;
import org.apache.flink.statefun.flink.io.generated.TargetFunctionType;
import org.apache.flink.statefun.playground.internal.io.Constants;

class IngressWebServer {
  private final Undertow server;

  IngressWebServer(int port, BlockingQueue<AutoRoutable> messageQueue) {
    this.server =
        Undertow.builder()
            .addHttpListener(port, "0.0.0.0")
            .setHandler(new IngressHttpHandler(messageQueue))
            .build();

    server.start();
  }

  void stop() {
    server.stop();
  }

  private static final class IngressHttpHandler implements HttpHandler {
    private final BlockingQueue<AutoRoutable> messageQueue;

    private IngressHttpHandler(BlockingQueue<AutoRoutable> messageQueue) {
      this.messageQueue = messageQueue;
    }

    @Override
    public void handleRequest(HttpServerExchange httpServerExchange) {
      httpServerExchange.getRequestReceiver().receiveFullBytes(this::onRequestBody);
    }

    private void onRequestBody(HttpServerExchange exchange, byte[] payload) {
      exchange.dispatch();

      try {
        final Address address = parseAddress(exchange.getRelativePath());
        final String typeUrl = parseTypeUrl(exchange.getRequestHeaders());

        final RoutingConfig routingConfig = createRoutingConfig(address, typeUrl);

        final AutoRoutable autoRoutable = createAutoRoutable(payload, address, routingConfig);

        messageQueue.put(autoRoutable);
        exchange.getResponseHeaders().put(Headers.STATUS, StatusCodes.OK);
      } catch (ParseException | InterruptedException e) {
        e.printStackTrace(System.out);
        exchange.getResponseHeaders().put(Headers.STATUS, StatusCodes.INTERNAL_SERVER_ERROR);
      }

      exchange.endExchange();
    }

    private AutoRoutable createAutoRoutable(
        byte[] payload, Address address, RoutingConfig routingConfig) {
      return AutoRoutable.newBuilder()
          .setId(address.key)
          .setConfig(routingConfig)
          .setPayloadBytes(ByteString.copyFrom(payload))
          .build();
    }

    private RoutingConfig createRoutingConfig(Address address, String typeUrl) {
      return RoutingConfig.newBuilder()
          .setTypeUrl(typeUrl)
          .addAllTargetFunctionTypes(
              Collections.singleton(
                  TargetFunctionType.newBuilder()
                      .setNamespace(address.namespace)
                      .setType(address.functionType)
                      .build()))
          .build();
    }

    private String parseTypeUrl(HeaderMap requestHeaders) {
      final HeaderValues headerValues = requestHeaders.get(Headers.CONTENT_TYPE);

      return headerValues.stream()
          .filter(value -> value.startsWith(Constants.STATEFUN_CONTENT_TYPE_PREFIX))
          .findFirst()
          .map(type -> type.substring(Constants.STATEFUN_CONTENT_TYPE_PREFIX.length()))
          .orElse(Constants.DEFAULT_INGRESS_TYPE);
    }

    private Address parseAddress(String relativePath) throws ParseException {
      final String[] split = relativePath.substring(1).split("/");

      if (split.length != 3) {
        throw new ParseException(
            "Invalid URL. Please specify '/namespace/function_type/function_id");
      }

      return new Address(split[0], split[1], split[2]);
    }
  }

  private static final class Address {
    final String namespace;
    final String functionType;
    final String key;

    private Address(String namespace, String functionType, String key) {
      this.namespace = namespace;
      this.functionType = functionType;
      this.key = key;
    }
  }
}

package org.apache.flink.statefun.playground.internal.io.flink;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.ByteString;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.statefun.playground.internal.io.Constants;
import org.apache.flink.statefun.sdk.reqreply.generated.TypedValue;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;

class PlaygroundEgress<T> extends RichSinkFunction<T> {
  private static final ObjectMapper objectMapper = new ObjectMapper();
  private static final RefCountedContainer<EgressWebServer> container = new RefCountedContainer<>();

  private final int port;
  private final Set<String> topics;

  private transient RefCountedContainer<EgressWebServer>.Lease handle;

  PlaygroundEgress(int port, Set<String> topics) {
    this.port = port;
    this.topics = new HashSet<>(topics);
    this.handle = null;
  }

  @Override
  public void open(Configuration parameters) throws Exception {
    super.open(parameters);
    this.handle = container.getOrCreate(() -> new EgressWebServer(port, topics));
  }

  @Override
  public void invoke(T value, Context context) throws Exception {
    final EgressRecord egressRecord = asEgressRecord(value);

    final String topic = egressRecord.getTopic();

    if (!topics.contains(topic)) {
      throw new IllegalArgumentException(
          String.format("Message was targeted to unknown topic %s.", topic));
    }

    handle.deref().offer(topic, ByteString.copyFromUtf8(egressRecord.getPayload()));
  }

  private EgressRecord asEgressRecord(T value) throws IOException {
    if (value instanceof TypedValue) {
      final TypedValue typedValue = ((TypedValue) value);

      if (typedValue.getTypename().equals(Constants.PLAYGROUND_EGRESS_RECORD)) {
        return objectMapper.readValue(typedValue.getValue().toByteArray(), EgressRecord.class);
      }
    }
    throw new IllegalArgumentException(String.format("Received unexpected value %s.", value));
  }

  @Override
  public void finish() throws Exception {
    super.finish();
    handle.close();
  }
}

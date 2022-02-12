package org.apache.flink.statefun.playground.internal.io.flink;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.statefun.flink.io.generated.AutoRoutable;
import org.apache.flink.streaming.api.functions.source.RichSourceFunction;

class PlaygroundIngress<T> extends RichSourceFunction<T> {

  private final int port;
  private final BlockingQueue<AutoRoutable> messageQueue;
  private transient IngressWebServer server;

  private volatile boolean running = true;

  PlaygroundIngress(int port) {
    this.port = port;
    this.messageQueue = new ArrayBlockingQueue<>(1 << 20);
  }

  @Override
  public void open(Configuration parameters) throws Exception {
    super.open(parameters);
    this.server = new IngressWebServer(port, messageQueue);
  }

  @Override
  public void run(SourceContext<T> sourceContext) throws Exception {
    while (running) {
      final AutoRoutable message = messageQueue.poll(50L, TimeUnit.MILLISECONDS);

      if (message != null) {
        synchronized (sourceContext.getCheckpointLock()) {
          sourceContext.collect((T) message);
        }
      }
    }

    if (server != null) {
      server.stop();
    }
  }

  @Override
  public void cancel() {
    running = false;
  }
}

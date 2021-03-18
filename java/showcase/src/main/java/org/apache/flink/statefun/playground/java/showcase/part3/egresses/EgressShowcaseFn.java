/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.flink.statefun.playground.java.showcase.part3.egresses;

import java.util.concurrent.CompletableFuture;
import org.apache.flink.statefun.playground.java.showcase.part4.storage.StateStorageShowcaseFn;
import org.apache.flink.statefun.sdk.java.Context;
import org.apache.flink.statefun.sdk.java.StatefulFunction;
import org.apache.flink.statefun.sdk.java.TypeName;
import org.apache.flink.statefun.sdk.java.io.KafkaEgressMessage;
import org.apache.flink.statefun.sdk.java.io.KinesisEgressMessage;
import org.apache.flink.statefun.sdk.java.message.Message;

/**
 *
 *
 * <h1>Showcase Part 3: Sending messages to egresses</h1>
 *
 * <p>To let your StateFun application interact with the outside world, functions may write messages
 * to egresses. This function demonstrates sending messages to an Apache Kafka or AWS Kinesis
 * egress, which is currently our most commonly used egresses that are natively supported by
 * StateFun.
 *
 * <h2>Next steps</h2>
 *
 * <p>Next, we recommend learning about StateFun's state storage management and how to access stored
 * values. Head over to {@link StateStorageShowcaseFn}!
 */
public final class EgressShowcaseFn implements StatefulFunction {

  /**
   * Egresses are associated with a unique {@link TypeName}. The typename is used to identify which
   * egress a message should be sent to.
   *
   * <p>StateFun currently has native support for using Apache Kafka topics or AWS Kinesis streams
   * as egresses.
   *
   * <p>Registering a Kafka or Kinesis egress under a given {@link TypeName} is beyond the scope of
   * what this specific part of the tutorial is attempting to demonstrate. For the time being, you
   * can simply assume that a Kafka egress has been registered under the typename {@literal
   * java.showcase.io/my-kafka-egress}, and a Kinesis egress has been registered under the typename
   * {@literal java.showcase.io/my-kinesis-egress}.
   */
  private static final TypeName KAFKA_EGRESS_TYPENAME =
      TypeName.typeNameOf("java.showcase.io", "my-kafka-egress");

  private static final TypeName KINESIS_EGRESS_TYPENAME =
      TypeName.typeNameOf("java.showcase.io", "my-kinesis-egress");

  @Override
  public CompletableFuture<Void> apply(Context context, Message message) {
    // there is a specific builder for messages to be sent to a Kafka egress ...
    context.send(
        KafkaEgressMessage.forEgress(KAFKA_EGRESS_TYPENAME)
            .withTopic("my-kafka-topic")
            .withUtf8Key("my-key")
            .withUtf8Value("hello world!")
            .build());

    // ... and also one for messages targeted at Kinesis egresses.
    context.send(
        KinesisEgressMessage.forEgress(KINESIS_EGRESS_TYPENAME)
            .withStream("my-kinesis-stream")
            .withUtf8PartitionKey("my-partition-key")
            .withUtf8ExplicitHashKey("my-explicit-hash-key")
            .withUtf8Value("hello world again!")
            .build());

    // likewise to messaging, writing to egresses are function side-effects that are non-blocking
    // and will be collected once the invocation returns.
    return context.done();
  }
}

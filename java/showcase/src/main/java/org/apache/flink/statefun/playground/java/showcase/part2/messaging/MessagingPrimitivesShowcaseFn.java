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

package org.apache.flink.statefun.playground.java.showcase.part2.messaging;

import static org.apache.flink.statefun.playground.java.showcase.part1.types.MyCustomTypes.USER_PROFILE_PROTOBUF_TYPE;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import org.apache.flink.statefun.playground.java.showcase.part1.types.generated.UserProfile;
import org.apache.flink.statefun.playground.java.showcase.part3.egresses.EgressShowcaseFn;
import org.apache.flink.statefun.sdk.java.Address;
import org.apache.flink.statefun.sdk.java.Context;
import org.apache.flink.statefun.sdk.java.StatefulFunction;
import org.apache.flink.statefun.sdk.java.TypeName;
import org.apache.flink.statefun.sdk.java.message.Message;
import org.apache.flink.statefun.sdk.java.message.MessageBuilder;

/**
 *
 *
 * <h1>Showcase Part 2: Messaging Primitives</h1>
 *
 * <p>This function demonstrates how to send messages to other functions.
 *
 * <h2>Next steps</h2>
 *
 * Besides sending messages to other functions, functions may also send messages to the outside
 * world via egresses. This is showcased by the next part of the series, {@link EgressShowcaseFn}.
 */
public final class MessagingPrimitivesShowcaseFn implements StatefulFunction {

  private static final TypeName TARGET_FN_TYPENAME =
      TypeName.typeNameOf("java.showcase.fns", "some-other-fn");

  @Override
  public CompletableFuture<Void> apply(Context context, Message message) {
    // You send messages to functions simply by specifying the target function's typename
    // and the target instance id for that for function; StateFun handles the routing for you,
    // without the need of any means for service discovery.
    final Address targetAddress = new Address(TARGET_FN_TYPENAME, "target-instance-id");

    // you can directly send primitive type values as messages, ...
    context.send(MessageBuilder.forAddress(targetAddress).withValue(123).build());
    context.send(MessageBuilder.forAddress(targetAddress).withValue(true).build());
    context.send(MessageBuilder.forAddress(targetAddress).withValue("hello world!").build());
    context.send(MessageBuilder.forAddress(targetAddress).withValue(3.14159e+11f).build());
    context.send(MessageBuilder.forAddress(targetAddress).withValue(3.14159).build());
    context.send(MessageBuilder.forAddress(targetAddress).withValue(-19911108123046639L).build());

    // ... or, in general, a value of any custom defined type.
    context.send(
        MessageBuilder.forAddress(targetAddress)
            .withCustomType(USER_PROFILE_PROTOBUF_TYPE, UserProfile.getDefaultInstance())
            .build());

    // You can send messages to any function, including yourself!
    context.send(MessageBuilder.forAddress(context.self()).withValue("hello world!").build());

    // Additionally, you may ask StateFun to send out a message after a specified delay.
    // A common usage pattern is to send delayed messages to yourself to model timer triggers.
    context.sendAfter(
        Duration.ofMinutes(10),
        MessageBuilder.forAddress(context.self()).withValue("hello world!").build());

    // None of the above sends is a blocking operation.
    // All side-effects (such as messaging other functions) are collected and happen after this
    // method returns.
    return context.done();
  }
}

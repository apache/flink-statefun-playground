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

package org.apache.flink.statefun.playground.java.greeter;

import static org.apache.flink.statefun.playground.java.greeter.types.Types.EGRESS_RECORD_JSON_TYPE;
import static org.apache.flink.statefun.playground.java.greeter.types.Types.USER_PROFILE_PROTOBUF_TYPE;

import java.util.concurrent.CompletableFuture;
import org.apache.flink.statefun.playground.java.greeter.types.EgressRecord;
import org.apache.flink.statefun.playground.java.greeter.types.generated.UserProfile;
import org.apache.flink.statefun.sdk.java.Context;
import org.apache.flink.statefun.sdk.java.StatefulFunction;
import org.apache.flink.statefun.sdk.java.StatefulFunctionSpec;
import org.apache.flink.statefun.sdk.java.TypeName;
import org.apache.flink.statefun.sdk.java.message.EgressMessageBuilder;
import org.apache.flink.statefun.sdk.java.message.Message;

/**
 * A simple function that computes personalized greetings messages based on a given {@link
 * UserProfile}. Then, it sends the greetings message back to the user via an egress Kafka topic.
 */
final class GreetingsFn implements StatefulFunction {
  private static final String[] GREETINGS_TEMPLATES =
      new String[] {"Welcome %s!", "Nice to see you again %s.", "Third time is a charm %s!"};

  static final TypeName TYPENAME = TypeName.typeNameOf("greeter.fns", "greetings");
  static final StatefulFunctionSpec SPEC =
      StatefulFunctionSpec.builder(TYPENAME).withSupplier(GreetingsFn::new).build();

  private static final TypeName PLAYGROUND_EGRESS =
      TypeName.typeNameOf("io.statefun.playground", "egress");

  @Override
  public CompletableFuture<Void> apply(Context context, Message message) {
    if (message.is(USER_PROFILE_PROTOBUF_TYPE)) {
      final UserProfile profile = message.as(USER_PROFILE_PROTOBUF_TYPE);
      final String greetings = createGreetingsMessage(profile);
      final EgressRecord egressRecord = new EgressRecord("greetings", greetings);

      context.send(
          EgressMessageBuilder.forEgress(PLAYGROUND_EGRESS)
              .withCustomType(EGRESS_RECORD_JSON_TYPE, egressRecord)
              .build());
    }
    return context.done();
  }

  private static String createGreetingsMessage(UserProfile profile) {
    final int seenCount = profile.getSeenCount();

    if (seenCount <= GREETINGS_TEMPLATES.length) {
      return String.format(GREETINGS_TEMPLATES[seenCount - 1], profile.getName());
    } else {
      return String.format(
          "Nice to see you for the %dth time, %s! It has been %d milliseconds since we last saw you.",
          seenCount, profile.getName(), profile.getLastSeenDeltaMs());
    }
  }
}

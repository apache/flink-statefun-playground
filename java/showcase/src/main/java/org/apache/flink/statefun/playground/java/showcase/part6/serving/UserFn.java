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

package org.apache.flink.statefun.playground.java.showcase.part6.serving;

import static org.apache.flink.statefun.playground.java.showcase.part1.types.MyCustomTypes.USER_LOGIN_JSON_TYPE;
import static org.apache.flink.statefun.playground.java.showcase.part1.types.MyCustomTypes.USER_PROFILE_PROTOBUF_TYPE;

import java.util.concurrent.CompletableFuture;
import org.apache.flink.statefun.playground.java.showcase.part1.types.UserLogin;
import org.apache.flink.statefun.playground.java.showcase.part1.types.generated.UserProfile;
import org.apache.flink.statefun.sdk.java.Context;
import org.apache.flink.statefun.sdk.java.StatefulFunction;
import org.apache.flink.statefun.sdk.java.StatefulFunctionSpec;
import org.apache.flink.statefun.sdk.java.TypeName;
import org.apache.flink.statefun.sdk.java.ValueSpec;
import org.apache.flink.statefun.sdk.java.message.Message;
import org.apache.flink.statefun.sdk.java.message.MessageBuilder;

/**
 * A stateful function that is invoked with {@link UserLogin} events, and persists in state storage
 * the number of times we've seen the user log in as well as the user's last seen timestamp. This
 * function in-turn invokes {@link GreetingsFn} with the user information to generate a personalized
 * greetings message.
 */
final class UserFn implements StatefulFunction {

  private static final ValueSpec<Integer> SEEN_COUNT = ValueSpec.named("seen_count").withIntType();
  private static final ValueSpec<Long> SEEN_TIMESTAMP_MS =
      ValueSpec.named("seen_timestamp_ms").withLongType();

  /**
   * Every registered function needs to be associated with an unique {@link TypeName}. A function's
   * typename is used by other functions and ingresses to have messages addressed to them.
   */
  static final TypeName TYPENAME = TypeName.typeNameOf("java.showcase.fns", "user");

  /**
   * Specification of the function. This can be used to register the function - see {@link
   * GreeterAppServer}.
   */
  static final StatefulFunctionSpec SPEC =
      StatefulFunctionSpec.builder(TYPENAME)
          .withValueSpecs(SEEN_COUNT, SEEN_TIMESTAMP_MS)
          .withSupplier(UserFn::new)
          .build();

  @Override
  public CompletableFuture<Void> apply(Context context, Message message) {
    if (message.is(USER_LOGIN_JSON_TYPE)) {
      final UserLogin login = message.as(USER_LOGIN_JSON_TYPE);

      int seenCount = context.storage().get(SEEN_COUNT).orElse(0);
      seenCount++;

      final long nowMs = System.currentTimeMillis();
      final long lastSeenTimestampMs = context.storage().get(SEEN_TIMESTAMP_MS).orElse(nowMs);

      context.storage().set(SEEN_COUNT, seenCount);
      context.storage().set(SEEN_TIMESTAMP_MS, nowMs);

      final UserProfile profile =
          UserProfile.newBuilder()
              .setName(login.getUserName())
              .setLoginLocation(login.getLoginType().name())
              .setSeenCount(seenCount)
              .setLastSeenDeltaMs(nowMs - lastSeenTimestampMs)
              .build();
      context.send(
          MessageBuilder.forAddress(GreetingsFn.TYPENAME, login.getUserId())
              .withCustomType(USER_PROFILE_PROTOBUF_TYPE, profile)
              .build());
    } else {
      throw new IllegalArgumentException("Unexpected message type: " + message.valueTypeName());
    }
    return context.done();
  }
}

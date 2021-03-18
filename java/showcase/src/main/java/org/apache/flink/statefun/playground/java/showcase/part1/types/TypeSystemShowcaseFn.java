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

package org.apache.flink.statefun.playground.java.showcase.part1.types;

import static org.apache.flink.statefun.playground.java.showcase.part1.types.MyCustomTypes.USER_LOGIN_JSON_TYPE;
import static org.apache.flink.statefun.playground.java.showcase.part1.types.MyCustomTypes.USER_PROFILE_PROTOBUF_TYPE;

import java.util.concurrent.CompletableFuture;
import org.apache.flink.statefun.playground.java.showcase.part1.types.generated.UserProfile;
import org.apache.flink.statefun.playground.java.showcase.part2.messaging.MessagingPrimitivesShowcaseFn;
import org.apache.flink.statefun.sdk.java.Context;
import org.apache.flink.statefun.sdk.java.StatefulFunction;
import org.apache.flink.statefun.sdk.java.TypeName;
import org.apache.flink.statefun.sdk.java.message.Message;
import org.apache.flink.statefun.sdk.java.types.Type;
import org.apache.flink.statefun.sdk.java.types.TypeSerializer;

/**
 *
 *
 * <h1>Showcase Part 1: Type System</h1>
 *
 * <p>This function demonstrates StateFun's type system using the Java SDK.
 *
 * <h2>Core {@link Type} abstraction</h2>
 *
 * <p>The core abstraction used by StateFun's type system is the {@link Type} interface, which
 * consists of a few things that StateFun uses to handle messages and state values:
 *
 * <ul>
 *   <li>A {@link TypeName} to identify the type.
 *   <li>A {@link TypeSerializer} for serializing and deserializing instances of the type.
 * </ul>
 *
 * <h2>Cross-language primitive types</h2>
 *
 * <p>StateFun's type system has cross-language support for common primitive types, such as boolean,
 * integer, long, etc. These primitive types have built-in {@link Type}s implemented for them
 * already, with predefined typenames.
 *
 * <p>This is of course all transparent for the user, so you don't need to worry about it. Functions
 * implemented in various languages (e.g. Java or Python) can message each other by directly sending
 * supported primitive values as message arguments. Moreover, the type system is used for state
 * values as well; so, you can expect that a function can safely read previous state after
 * reimplementing it in a different language. We'll cover more on state storage access in later
 * parts of the showcase series.
 *
 * <h2>Common custom types (e.g. JSON or Protobuf)</h2>
 *
 * <p>The type system is also very easily extensible to support custom message types, such as JSON
 * or Protobuf messages. This is just a matter of implementing your own {@link Type} with a custom
 * typename and serializer.
 *
 * <p>StateFun makes this super easy by providing builder utilities to help you create a simple
 * {@link Type}. Take a look at {@link MyCustomTypes#USER_LOGIN_JSON_TYPE} and {@link
 * MyCustomTypes#USER_PROFILE_PROTOBUF_TYPE} on recommended ways to quickly create a StateFun {@link
 * Type} for your JSON or Protobuf messages.
 *
 * <h2>Next steps</h2>
 *
 * Next, we recommend learning about the messaging primitives StateFun provides. Head over to {@link
 * MessagingPrimitivesShowcaseFn}!
 */
public final class TypeSystemShowcaseFn implements StatefulFunction {

  @Override
  public CompletableFuture<Void> apply(Context context, Message message) {
    // All values, including messages and storage values, are handled via StateFun's type system.
    // StateFun ships built-in primitive types that handles de-/serialization of messages across
    // functions:
    if (message.isBoolean()) {
      final boolean val = message.asBoolean();
    } else if (message.isInt()) {
      final int val = message.asInt();
    } else if (message.isFloat()) {
      final float val = message.asFloat();
    } else if (message.isLong()) {
      final long val = message.asLong();
    } else if (message.isDouble()) {
      final double val = message.asDouble();
    } else if (message.isUtf8String()) {
      final String val = message.asUtf8String();
    } else if (message.is(USER_LOGIN_JSON_TYPE)) {
      // You can also define your own types using the type system, such as a JSON message, ...
      final UserLogin val = message.as(USER_LOGIN_JSON_TYPE);
    } else if (message.is(USER_PROFILE_PROTOBUF_TYPE)) {
      // ... or a Protobuf message!
      final UserProfile val = message.as(USER_PROFILE_PROTOBUF_TYPE);
    } else {
      throw new IllegalArgumentException("Unrecognized message type!");
    }
    return context.done();
  }
}

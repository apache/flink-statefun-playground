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

package org.apache.flink.statefun.playground.java.showcase.part4.storage;

import static org.apache.flink.statefun.playground.java.showcase.part1.types.MyCustomTypes.USER_PROFILE_PROTOBUF_TYPE;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.apache.flink.statefun.playground.java.showcase.part1.types.generated.UserProfile;
import org.apache.flink.statefun.playground.java.showcase.part5.asyncops.AsyncOpsShowcaseFn;
import org.apache.flink.statefun.sdk.java.AddressScopedStorage;
import org.apache.flink.statefun.sdk.java.Context;
import org.apache.flink.statefun.sdk.java.StatefulFunction;
import org.apache.flink.statefun.sdk.java.ValueSpec;
import org.apache.flink.statefun.sdk.java.message.Message;

/**
 *
 *
 * <h1>Showcase Part 4: Function state storage</h1>
 *
 * <p>One of StateFun's most powerful features is its capability of managing function state in a
 * consistent and fault-tolerant manner. This function demonstrates accessing and manipulating
 * persisted function state that is managed by StateFun.
 *
 * <h2>Next steps</h2>
 *
 * In some applications, functions may need to retrieve existing data from external services. Take a
 * look at the next series in this showcase, {@link AsyncOpsShowcaseFn}, on how to perform
 * asynchronous operations in your functions.
 */
public class StateStorageShowcaseFn implements StatefulFunction {
  public static final ValueSpec<Integer> INT_VALUE = ValueSpec.named("int_value").withIntType();
  public static final ValueSpec<Boolean> BOOL_VALUE =
      ValueSpec.named("bool_value").thatExpiresAfterCall(Duration.ofHours(5)).withBooleanType();
  public static final ValueSpec<UserProfile> USER_PROFILE_VALUE =
      ValueSpec.named("user_profile_value").withCustomType(USER_PROFILE_PROTOBUF_TYPE);

  @Override
  public CompletableFuture<Void> apply(Context context, Message message) {
    // each function invocation gets access to storage that is scoped to the current function
    // instance's address, i.e. (function typename, instance id). For example, if (UserFn, "Gordon")
    // was invoked, the values you get access to belongs specifically to user Gordon.
    final AddressScopedStorage storage = context.storage();

    // you can think of the storage having several "columns", with each column identified by an
    // uniquely named ValueSpec. Using the ValueSpec instances, you can access the value of
    // individual columns.
    final Optional<Integer> storedInt = storage.get(INT_VALUE);
    final Optional<Boolean> storedBoolean = storage.get(BOOL_VALUE);
    final Optional<UserProfile> storedUserProfile = storage.get(USER_PROFILE_VALUE);

    // the ValueSpec instance is also used for manipulating the stored values, e.g. updating ...
    storage.set(INT_VALUE, storedInt.orElse(0) + 1);
    storage.set(BOOL_VALUE, !storedBoolean.orElse(true));

    // ... or clearing the value!
    storage.remove(USER_PROFILE_VALUE);

    return context.done();
  }
}

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

package org.apache.flink.statefun.playground.java.showcase.part5.asyncops;

import static org.apache.flink.statefun.playground.java.showcase.part1.types.MyCustomTypes.USER_PROFILE_PROTOBUF_TYPE;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.apache.flink.statefun.playground.java.showcase.part1.types.generated.UserProfile;
import org.apache.flink.statefun.playground.java.showcase.part6.serving.GreeterAppServer;
import org.apache.flink.statefun.sdk.java.Context;
import org.apache.flink.statefun.sdk.java.StatefulFunction;
import org.apache.flink.statefun.sdk.java.ValueSpec;
import org.apache.flink.statefun.sdk.java.message.Message;

/**
 *
 *
 * <h1>Showcase Part 5: Asynchronous operations</h1>
 *
 * <p>This function demonstrates performing asynchronous operations during a function invocation. It
 * is a common scenario for functions to have external dependencies in order for it to complete its
 * work, such as fetching enrichment information from remote databases.
 *
 * <h2>Next steps</h2>
 *
 * After learning everything about implementing a stateful function, the last bit of this showcase
 * series demonstrates how to expose the functions you implemented so that the StateFun runtime can
 * reach them and dispatch message invocations. Take a look now at the {@link GreeterAppServer}.
 */
public final class AsyncOpsShowcaseFn implements StatefulFunction {

  private static final ValueSpec<UserProfile> USER_PROFILE_VALUE =
      ValueSpec.named("user_profile").withCustomType(USER_PROFILE_PROTOBUF_TYPE);

  @Override
  public CompletableFuture<Void> apply(Context context, Message message) {
    final Optional<UserProfile> storedUserProfile = context.storage().get(USER_PROFILE_VALUE);

    if (!storedUserProfile.isPresent()) {
      final String username = context.self().id();

      final CompletableFuture<Integer> ageResult = getAgeFromRemoteDatabase(username);
      final CompletableFuture<List<String>> friendsResult =
          getFriendsListFromAnotherRemoteDatabase(username);

      return CompletableFuture.allOf(ageResult, friendsResult)
          .whenComplete(
              // Upon completion of the various async operations, write our enriched user profile to
              // be persisted in StateFun's state storage.
              (ignored, exception) -> {
                final UserProfile profile =
                    UserProfile.newBuilder()
                        .setAge(ageResult.join())
                        .addAllFriend(friendsResult.join())
                        .build();
                context.storage().set(USER_PROFILE_VALUE, profile);
              });
    }
    return context.done();
  }

  /** A mock asynchronous request to fetch a user's age information from a remote database. */
  private CompletableFuture<Integer> getAgeFromRemoteDatabase(String username) {
    return CompletableFuture.supplyAsync(
        () -> {
          try {
            Thread.sleep(1000);
          } catch (InterruptedException e) {
            throw new RuntimeException();
          }
          return 29;
        });
  }

  /** A mock asynchronouse request to fetch a user's friends list from a remote database */
  private CompletableFuture<List<String>> getFriendsListFromAnotherRemoteDatabase(String userName) {
    return CompletableFuture.supplyAsync(
        () -> {
          try {
            Thread.sleep(1000);
          } catch (InterruptedException e) {
            throw new RuntimeException();
          }
          return Arrays.asList("Igal", "Marta", "Stephan", "Seth", "Konstantin");
        });
  }
}

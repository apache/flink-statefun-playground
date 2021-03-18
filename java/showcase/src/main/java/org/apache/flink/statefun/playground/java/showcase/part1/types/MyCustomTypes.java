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

import com.google.protobuf.Descriptors;
import com.google.protobuf.Parser;
import org.apache.flink.statefun.playground.java.showcase.part1.types.generated.UserProfile;
import org.apache.flink.statefun.sdk.java.types.Type;

public final class MyCustomTypes {

  /**
   * See {@link TypeUtils#createJsonType(String, Class)} for the recommended way of creating a
   * {@link Type} for your JSON messages.
   */
  public static final Type<UserLogin> USER_LOGIN_JSON_TYPE =
      TypeUtils.createJsonType("java.showcase.types", UserLogin.class);

  /**
   * See {@link TypeUtils#createProtobufType(String, Descriptors.Descriptor, Parser)} for the
   * recommended way of creating a {@link Type} for your JSON messages.
   */
  public static final Type<UserProfile> USER_PROFILE_PROTOBUF_TYPE =
      TypeUtils.createProtobufType(
          "java.showcase.types", UserProfile.getDescriptor(), UserProfile.parser());
}

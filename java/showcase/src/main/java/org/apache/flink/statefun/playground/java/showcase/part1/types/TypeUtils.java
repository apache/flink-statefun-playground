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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.Descriptors;
import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.Parser;
import org.apache.flink.statefun.sdk.java.TypeName;
import org.apache.flink.statefun.sdk.java.types.SimpleType;
import org.apache.flink.statefun.sdk.java.types.Type;

final class TypeUtils {

  private TypeUtils() {}

  private static final ObjectMapper JSON_OBJ_MAPPER = new ObjectMapper();

  static <T> Type<T> createJsonType(String typeNamespace, Class<T> jsonClass) {
    return SimpleType.simpleImmutableTypeFrom(
        TypeName.typeNameOf(typeNamespace, jsonClass.getName()),
        JSON_OBJ_MAPPER::writeValueAsBytes,
        bytes -> JSON_OBJ_MAPPER.readValue(bytes, jsonClass));
  }

  static <T extends GeneratedMessageV3> Type<T> createProtobufType(
      String typeNamespace, Descriptors.Descriptor protobufDescriptor, Parser<T> protobufParser) {
    return SimpleType.simpleImmutableTypeFrom(
        TypeName.typeNameOf(typeNamespace, protobufDescriptor.getFullName()),
        T::toByteArray,
        protobufParser::parseFrom);
  }
}

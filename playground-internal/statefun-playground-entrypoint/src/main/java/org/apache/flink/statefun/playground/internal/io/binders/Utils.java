package org.apache.flink.statefun.playground.internal.io.binders;

import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.JsonNode;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.statefun.extensions.ComponentJsonObject;
import org.apache.flink.statefun.flink.common.json.StateFunObjectMapper;
import org.apache.flink.statefun.sdk.TypeName;

public class Utils {
  private static final ObjectMapper OBJECT_MAPPER = StateFunObjectMapper.create();

  private Utils() {
    throw new UnsupportedOperationException("Should not be instantiated.");
  }

  public static void validateComponent(ComponentJsonObject component, TypeName expected) {
    final TypeName typeName = component.binderTypename();

    if (!typeName.equals(expected)) {
      throw new IllegalArgumentException(
          String.format("Binder handles types %s but was called for type %s.", expected, typeName));
    }
  }

  public static <T> T parseJson(JsonNode jsonNode, Class<T> clazz) {
    try {
      return OBJECT_MAPPER.treeToValue(jsonNode, clazz);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(
          String.format("Could not parse the %s from %s.", clazz.getSimpleName(), jsonNode));
    }
  }
}

package org.apache.flink.types;

import org.apache.flink.statefun.sdk.java.TypeName;
import org.apache.flink.statefun.sdk.java.types.SimpleType;
import org.apache.flink.statefun.sdk.java.types.Type;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Types {
    private static final ObjectMapper mapper = new ObjectMapper();

    /* ingress -> word-count */
    public static final Type<AddNewText> ADD_NEW_TEXT =
        SimpleType.simpleImmutableTypeFrom(
            TypeName.typeNameFromString("statefun.testbed.types/AddNewText"),
            mapper::writeValueAsBytes,
            bytes -> mapper.readValue(bytes, AddNewText.class));

    /* word-count -> egress */


    public static class AddNewText {
        private final String text;

        @JsonCreator
        public AddNewText(
                @JsonProperty("text") String text) {
            this.text = text;
        }

        public String getText() {
            return text;
        }

        @Override
        public String toString() {
            return "AddNewText{" +
                    "text='" + text + '\'' +
                    '}';
        }
    }
}

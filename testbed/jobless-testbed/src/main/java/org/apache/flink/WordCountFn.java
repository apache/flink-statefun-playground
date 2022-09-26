package org.apache.flink;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.apache.flink.statefun.sdk.java.AddressScopedStorage;
import org.apache.flink.statefun.sdk.java.Context;
import org.apache.flink.statefun.sdk.java.StatefulFunction;
import org.apache.flink.statefun.sdk.java.TypeName;
import org.apache.flink.statefun.sdk.java.ValueSpec;
import org.apache.flink.statefun.sdk.java.message.Message;
import org.apache.flink.statefun.sdk.java.types.SimpleType;
import org.apache.flink.statefun.sdk.java.types.Type;
import org.apache.flink.types.Types;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

public class WordCountFn implements StatefulFunction {
    private static final Logger LOG = LoggerFactory.getLogger(WordCountFn.class);
    static final TypeName TYPE = TypeName.typeNameFromString("statefun.testbed.fns/word-count");
    static final ValueSpec<WordFrequency> WORD_FREQUENCY = ValueSpec.named("word_frequency").withCustomType(WordFrequency.TYPE);

    @Override
    public CompletableFuture<Void> apply(Context context, Message message) {
        if (message.is(Types.ADD_NEW_TEXT)) {
            final Types.AddNewText addNewText = message.as(Types.ADD_NEW_TEXT);
            LOG.info("{}", addNewText);
            LOG.info("Scope: {}", context.self());
            LOG.info("Caller: {}", context.caller());

            final AddressScopedStorage storage = context.storage();
            final WordFrequency wordFrequency = storage.get(WORD_FREQUENCY).orElse(WordFrequency.initEmpty());

            updateWordFrequency(wordFrequency, addNewText.getText());

            storage.set(WORD_FREQUENCY, wordFrequency);
            LOG.info("Word frequency: {}", wordFrequency);
            LOG.info("---");
        }

        return context.done();
    }

    private void updateWordFrequency(WordFrequency wordFrequency, String text) {
        final String[] words = text.split(" ");
        for (String word : words) {
            wordFrequency.updateOrElseCreate(word, 1);
        }
    }

    private static class WordFrequency {
        private static final ObjectMapper mapper = new ObjectMapper();
        static final Type<WordFrequency> TYPE = SimpleType.simpleImmutableTypeFrom(
                TypeName.typeNameFromString("statefun.testbed.fns/WordFrequency"),
                mapper::writeValueAsBytes,
                bytes -> mapper.readValue(bytes, WordFrequency.class));

        @JsonProperty("word_frequency")
        private final Map<String, Integer> wordFrequency;

        public static WordFrequency initEmpty() {
            return new WordFrequency(new HashMap<>());
        }

        @JsonCreator
        public WordFrequency(@JsonProperty("word_frequency") Map<String, Integer> wordFrequency) {
            this.wordFrequency = wordFrequency;
        }

        public void updateOrElseCreate(String word, Integer frequency) {
            wordFrequency.put(word, wordFrequency.getOrDefault(word, 0) + frequency);
        }

        @JsonIgnore
        public Set<Map.Entry<String, Integer>> getEntries() {
            return wordFrequency.entrySet();
        }

        @JsonProperty("word_frequency")
        public Map<String, Integer> getWordFrequency() {
            return wordFrequency;
        };

        public void clear() {
            wordFrequency.clear();
        }

        @Override
        public String toString() {
            return "WordFrequency{" + "wordFrequency=" + wordFrequency + '}';
        }
    }
}

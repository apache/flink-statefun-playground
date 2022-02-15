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
package org.apache.flink.statefun.playground.java.shoppingcart;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import org.apache.flink.statefun.sdk.java.Address;
import org.apache.flink.statefun.sdk.java.AddressScopedStorage;
import org.apache.flink.statefun.sdk.java.Context;
import org.apache.flink.statefun.sdk.java.StatefulFunction;
import org.apache.flink.statefun.sdk.java.TypeName;
import org.apache.flink.statefun.sdk.java.ValueSpec;
import org.apache.flink.statefun.sdk.java.message.EgressMessage;
import org.apache.flink.statefun.sdk.java.message.EgressMessageBuilder;
import org.apache.flink.statefun.sdk.java.message.Message;
import org.apache.flink.statefun.sdk.java.message.MessageBuilder;
import org.apache.flink.statefun.sdk.java.types.SimpleType;
import org.apache.flink.statefun.sdk.java.types.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class UserShoppingCartFn implements StatefulFunction {

  private static final Logger LOG = LoggerFactory.getLogger(UserShoppingCartFn.class);

  static final TypeName TYPE = TypeName.typeNameFromString("com.example/user-shopping-cart");
  static final ValueSpec<Basket> BASKET = ValueSpec.named("basket").withCustomType(Basket.TYPE);

  @Override
  public CompletableFuture<Void> apply(Context context, Message message) {
    if (message.is(Messages.ADD_TO_CART)) {
      final Messages.AddToCart addToCart = message.as(Messages.ADD_TO_CART);
      LOG.info("{}", addToCart);
      LOG.info("Scope: {}", context.self());
      LOG.info("Caller: {}", context.caller());

      final Messages.RequestItem requestItem = new Messages.RequestItem(addToCart.getQuantity());
      final Message request =
          MessageBuilder.forAddress(StockFn.TYPE, addToCart.getItemId())
              .withCustomType(Messages.REQUEST_ITEM_TYPE, requestItem)
              .build();
      context.send(request);
      LOG.info("---");
      return context.done();
    }

    if (message.is(Messages.ITEM_AVAILABILITY_TYPE)) {
      final Messages.ItemAvailability availability = message.as(Messages.ITEM_AVAILABILITY_TYPE);
      LOG.info("{}", availability);
      LOG.info("Scope: {}", context.self());
      LOG.info("Caller: {}", context.caller());
      if (Messages.ItemAvailability.Status.INSTOCK.equals(availability.getStatus())) {
        final AddressScopedStorage storage = context.storage();
        final Basket basket = storage.get(BASKET).orElse(Basket.initEmpty());

        // ItemAvailability event comes from the Stock function and contains the itemId as the
        // caller id
        final Optional<Address> caller = context.caller();
        if (caller.isPresent()) {
          basket.add(caller.get().id(), availability.getQuantity());
        } else {
          throw new IllegalStateException("There should always be a caller in this example");
        }

        storage.set(BASKET, basket);
        LOG.info("Basket: {}", basket);
        LOG.info("---");
      }
      return context.done();
    }

    if (message.is(Messages.CLEAR_CART_TYPE)) {
      final Messages.ClearCart clear = message.as(Messages.CLEAR_CART_TYPE);
      final AddressScopedStorage storage = context.storage();

      LOG.info("{}", clear);
      LOG.info("Scope: {}", context.self());
      LOG.info("Caller: {}", context.caller());
      LOG.info("Basket: {}", storage.get(BASKET));

      storage
          .get(BASKET)
          .ifPresent(
              basket -> {
                for (Map.Entry<String, Integer> entry : basket.getEntries()) {
                  Messages.RestockItem restockItem =
                      new Messages.RestockItem(entry.getKey(), entry.getValue());
                  Message restockCommand =
                      MessageBuilder.forAddress(StockFn.TYPE, entry.getKey())
                          .withCustomType(Messages.RESTOCK_ITEM_TYPE, restockItem)
                          .build();

                  context.send(restockCommand);
                }
                basket.clear();
              });
      LOG.info("---");
      return context.done();
    }

    if (message.is(Messages.CHECKOUT_TYPE)) {
      final Messages.Checkout checkout = message.as(Messages.CHECKOUT_TYPE);
      final AddressScopedStorage storage = context.storage();

      LOG.info("{}", checkout);
      LOG.info("Scope: {}", context.self());
      LOG.info("Caller: {}", context.caller());
      LOG.info("Basket: {}", storage.get(BASKET));

      final Optional<String> itemsOption =
          storage
              .get(BASKET)
              .map(
                  basket ->
                      basket.getEntries().stream()
                          .map(entry -> entry.getKey() + ": " + entry.getValue())
                          .collect(Collectors.joining("\n")));

      itemsOption.ifPresent(
          items -> {
            LOG.info("Receipt items: ");
            LOG.info("{}", items);

            final Messages.Receipt receipt = new Messages.Receipt(context.self().id(), items);
            final EgressMessage egressMessage =
                EgressMessageBuilder.forEgress(Identifiers.RECEIPT_EGRESS)
                    .withCustomType(
                        Messages.EGRESS_RECORD_JSON_TYPE,
                        new Messages.EgressRecord(Identifiers.RECEIPT_TOPICS, receipt.toString()))
                    .build();
            context.send(egressMessage);
          });
      LOG.info("---");
    }
    return context.done();
  }

  private static class Basket {

    private static final ObjectMapper mapper = new ObjectMapper();

    public static final Type<Basket> TYPE =
        SimpleType.simpleImmutableTypeFrom(
            TypeName.typeNameFromString("com.example/Basket"),
            mapper::writeValueAsBytes,
            bytes -> mapper.readValue(bytes, Basket.class));

    @JsonProperty("basket")
    private final Map<String, Integer> basket;

    public static Basket initEmpty() {
      return new Basket(new HashMap<>());
    }

    @JsonCreator
    public Basket(@JsonProperty("basket") Map<String, Integer> basket) {
      this.basket = basket;
    }

    public void add(String itemId, int quantity) {
      basket.put(itemId, basket.getOrDefault(itemId, 0) + quantity);
    }

    public void remove(String itemId, int quantity) {
      int remainder = basket.getOrDefault(itemId, 0) - quantity;
      if (remainder > 0) {
        basket.put(itemId, remainder);
      } else {
        basket.remove(itemId);
      }
    }

    @JsonIgnore
    public Set<Map.Entry<String, Integer>> getEntries() {
      return basket.entrySet();
    }

    @JsonProperty("basket")
    public Map<String, Integer> getBasketContent() {
      return basket;
    };

    public void clear() {
      basket.clear();
    }

    @Override
    public String toString() {
      return "Basket{" + "basket=" + basket + '}';
    }
  }
}

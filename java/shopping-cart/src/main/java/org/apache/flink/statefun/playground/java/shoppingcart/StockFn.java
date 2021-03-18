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

import static org.apache.flink.statefun.playground.java.shoppingcart.Messages.*;
import static org.apache.flink.statefun.playground.java.shoppingcart.Messages.ItemAvailability.*;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.apache.flink.statefun.sdk.java.Address;
import org.apache.flink.statefun.sdk.java.AddressScopedStorage;
import org.apache.flink.statefun.sdk.java.Context;
import org.apache.flink.statefun.sdk.java.StatefulFunction;
import org.apache.flink.statefun.sdk.java.TypeName;
import org.apache.flink.statefun.sdk.java.ValueSpec;
import org.apache.flink.statefun.sdk.java.message.Message;
import org.apache.flink.statefun.sdk.java.message.MessageBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class StockFn implements StatefulFunction {

  private static final Logger LOG = LoggerFactory.getLogger(UserShoppingCartFn.class);

  static final TypeName TYPE = TypeName.typeNameFromString("com.example/stock");
  static final ValueSpec<Integer> STOCK = ValueSpec.named("stock").withIntType();

  @Override
  public CompletableFuture<Void> apply(Context context, Message message) {
    AddressScopedStorage storage = context.storage();
    final int quantity = storage.get(STOCK).orElse(0);
    if (message.is(RESTOCK_ITEM_TYPE)) {
      RestockItem restock = message.as(RESTOCK_ITEM_TYPE);

      LOG.info("{}", restock);
      LOG.info("Scope: {}", context.self());
      LOG.info("Caller: {}", context.caller());

      final int newQuantity = quantity + restock.getQuantity();
      storage.set(STOCK, newQuantity);
      LOG.info("---");
      return context.done();
    } else if (message.is(REQUEST_ITEM_TYPE)) {
      final RequestItem request = message.as(REQUEST_ITEM_TYPE);
      LOG.info("{}", request);
      LOG.info("Scope: {}", context.self());
      LOG.info("Caller: {}", context.caller());

      final int requestQuantity = request.getQuantity();

      final ItemAvailability itemAvailability;
      LOG.info("Available quantity: {}", quantity);
      LOG.info("Requested quantity: {}", requestQuantity);
      if (quantity >= requestQuantity) {
        storage.set(STOCK, quantity - requestQuantity);
        itemAvailability = new ItemAvailability(Status.INSTOCK, requestQuantity);
      } else {
        itemAvailability = new ItemAvailability(Status.OUTOFSTOCK, requestQuantity);
      }

      final Optional<Address> caller = context.caller();
      if(caller.isPresent()){
        context.send( MessageBuilder.forAddress(caller.get())
                .withCustomType(ITEM_AVAILABILITY_TYPE, itemAvailability)
                .build());
      } else {
        throw new IllegalStateException("There should always be a caller in this example");
      }
      LOG.info("---");
    }
    return context.done();
  }
}

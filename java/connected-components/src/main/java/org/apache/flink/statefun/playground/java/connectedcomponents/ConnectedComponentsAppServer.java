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

package org.apache.flink.statefun.playground.java.connectedcomponents;

import io.undertow.Undertow;
import org.apache.flink.statefun.playground.java.connectedcomponents.undertow.UndertowHttpHandler;
import org.apache.flink.statefun.sdk.java.StatefulFunctions;
import org.apache.flink.statefun.sdk.java.handler.RequestReplyHandler;

/**
 * Entry point to start an {@link Undertow} web server that exposes the functions that build up our
 * connected components application {@link ConnectedComponentsFn}.
 *
 * <p>Here we are using the {@link Undertow} web server just to show a very simple demonstration.
 * You may choose any web server that can handle HTTP request and responses, for example, Spring,
 * Micronaut, or even deploy your functions on popular FaaS platforms, like AWS Lambda.
 */
public final class ConnectedComponentsAppServer {

  public static void main(String[] args) {
    final StatefulFunctions functions = new StatefulFunctions();
    functions.withStatefulFunction(ConnectedComponentsFn.SPEC);

    final RequestReplyHandler requestReplyHandler = functions.requestReplyHandler();
    final Undertow httpServer =
        Undertow.builder()
            .addHttpListener(1108, "0.0.0.0")
            .setHandler(new UndertowHttpHandler(requestReplyHandler))
            .build();
    httpServer.start();
  }
}

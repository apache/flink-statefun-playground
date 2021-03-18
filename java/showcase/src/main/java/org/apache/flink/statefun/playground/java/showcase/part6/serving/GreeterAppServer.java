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

package org.apache.flink.statefun.playground.java.showcase.part6.serving;

import io.undertow.Undertow;
import org.apache.flink.statefun.sdk.java.StatefulFunctions;
import org.apache.flink.statefun.sdk.java.handler.RequestReplyHandler;

/**
 *
 *
 * <h1>Showcase Part 6: Serving your stateful functions</h1>
 *
 * <p>This is the last part of the StateFun Java SDK showcase tutorial! In this section, we'd like
 * to guide you through how to expose the functions you implemented via any HTTP web server
 * framework you prefer. This class is a standalone Java program that can be ran directly in the
 * IDE, and exposes some functions using an {@link Undertow} web server. Here we are using the
 * {@link Undertow} web server just to show a very simple demonstration. * You may choose any web
 * server that can handle HTTP request and responses, for example, Spring, * Micronaut, or even
 * deploy your functions on popular FaaS platforms, like AWS Lambda.
 *
 * <p>In addition to that, you will also run a "toy" set of StateFun runtime processes in a separate
 * program to see StateFun in action for the first time. The StateFun processes are responsible for
 * routing and dispatching the messages from ingresses, functions and egresses, and also manages the
 * function state storages to be consistent and fault-tolerant. This is all done transparently in
 * the background!
 *
 * <h2>Next steps</h2>
 *
 * <p>The setup you executed in this tutorial is NOT how you'd normally deploy StateFun processes
 * and functions. It's a rather simplified setup to allow you to explore the interaction between
 * functions and the StateFun processes by setting debugger breakpoints in the function code.
 *
 * <p>We recommend now to take a look at a slightly more realistic setup, using docker-compose, in
 * the <a href="https://github.com/apache/flink-statefun-playground/java/greeter/">Greeter
 * Docker-Compose example</a>.
 */
public final class GreeterAppServer {

  public static final int PORT = 1108;

  /**
   * Run this program to start a web server that handles invocations for two very simple functions
   * that build up an User Greeter application, {@link UserFn} and {@link GreetingsFn}! The former
   * keeps track of user information in the state storage, and passes that information to the latter
   * to generate personalized greeting messages to be sent to users via a Kafka egress.
   *
   * <p>This program can be ran directly in the IDE (i.e. right click on the class and hit "Run
   * main()"!). After starting this functions server, head over to {@link
   * StatefulFunctionsRuntimeProcesses} to start the StateFun processes on the side.
   *
   * <p>If you want to take a closer look and explore the interaction between the StateFun processes
   * and the served functions, you may also set breakpoints in {@link UserFn} or {@link GreetingsFn}
   * and run this program in Debug mode.
   */
  public static void main(String[] args) {

    // Register the functions that you want to serve ...
    final StatefulFunctions functions = new StatefulFunctions();
    functions.withStatefulFunction(UserFn.SPEC);
    functions.withStatefulFunction(GreetingsFn.SPEC);

    // ... and build a request-reply handler for the registered functions, which understands how to
    // decode invocation requests dispatched from StateFun / encode side-effects (e.g. state storage
    // updates, or invoking other functions) as responses to be handled by StateFun.
    final RequestReplyHandler requestReplyHandler = functions.requestReplyHandler();

    // Use the request-reply handler along with your favorite HTTP web server framework
    // to serve the functions!
    final Undertow httpServer =
        Undertow.builder()
            .addHttpListener(PORT, "localhost")
            .setHandler(new UndertowHttpHandler(requestReplyHandler))
            .build();
    httpServer.start();
  }
}

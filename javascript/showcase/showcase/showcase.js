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
"use strict";

const http = require("http");
const {StateFun, Message, Context, messageBuilder, kafkaEgressMessage, kinesisEgressMessage, egressMessageBuilder} = require("apache-flink-statefun");

const {GreetJsonType, UserProfileProtobufType} = require("./showcase_custom_types");


const statefun = new StateFun();

/**
 *  Showcase Part 1: Type System
 *  ============================
 *  This function demonstrates StateFun's type system using the JavaScript SDK.
 *
 *  Core Type abstraction
 *  =====================
 *  The core abstraction used by StateFun's type system is the Type interface, which
 *  consists of a few things that StateFun uses to handle messages and state values:
 *
 *  A TypeName to identify the type.
 *  A TypeSerializer for serializing and deserializing instances of the type.
 *
 *  Cross-language primitive types
 *  ==============================
 *  StateFun's type system has cross-language support for common primitive types, such as boolean,
 *  integer, long, etc. These primitive types have built-in Types implemented for them
 *  already, with predefined typenames.
 *
 *  This is of course all transparent for the user, so you don't need to worry about it. Functions
 *  implemented in various languages (e.g. Java or Python) can message each other by directly sending
 *  supported primitive values as message arguments. Moreover, the type system is used for state
 *  values as well; so, you can expect that a function can safely read previous state after
 *  reimplementing it in a different language. We'll cover more on state storage access in later
 *  parts of the showcase series.
 *
 *  Common custom types (e.g. JSON or Protobuf)
 *  ===========================================
 *  The type system is also very easily extensible to support custom message types, such as JSON
 *  or Protobuf messages. This is just a matter of implementing your own Type with a custom
 *  typename and serializer.
 */
statefun.bind({
    typename: "showcase/types",
    fn(context, message) {
        /**
         * All values, including messages and storage values, are handled via StateFun's type system.
         * StateFun ships built-in primitive types that handles de-/serialization of messages across
         * functions:
         */
        if (message.isInt()) {
            console.log(`I'm an int ${message.asInt()}`);
        } else if (message.isFloat()) {
            console.log(`I'm a float ${message.asFloat()}`);
        } else if (message.isString()) {
            console.log(`I'm a string ${message.asString()}`);
        } else if (message.isBoolean()) {
            console.log(`I'm a boolean ${message.asBoolean()}`);
        } else if (message.is(GreetJsonType)) {
            // You can also define your own types using the type system, such as a JSON message ...
            const val = message.as(GreetJsonType);
            console.log(val);
        } else if (message.is(UserProfileProtobufType)) {
            // And even protobuf!
            const val = message.as(UserProfileProtobufType);
            console.log(val);
        }
    }
});


/**
 *  Showcase Part 2: Messaging Primitives
 *  =====================================
 *  This function demonstrates how to send messages to other functions.
 *
 */
statefun.bind({
    typename: "showcase/messaging",
    fn(context, message) {

        // You send messages to functions simply by specifying the target function's typename
        // and the target instance id for that for function; StateFun handles the routing for you,
        // without the need of any means for service discovery.
        const target = "showcase/types"

        // # you can directly send primitive type values as messages, ...
        context.send(messageBuilder({typename: target, id: "0", value: "Hello there!"}));

        // ... or, in general, a value of any custom defined type.
        context.send(
            messageBuilder({typename: target, id: "6", value: {"name": "Joe"}, valueType: GreetJsonType}));

        // or extract the payload directly
        console.log(`The payload's type is ${message.valueTypeName} and the raw bytes are ${message.rawValueBytes}`);

        // You can send messages to any function, including yourself!
        const me = context.self;
        context.send(messageBuilder({typename: me.typename, id: me.id, value: "hello!"}));

        // Additionally, you may ask StateFun to send out a message after a specified delay.
        // A common usage pattern is to send delayed messages to yourself to model timer triggers.
        context.sendAfter(1_000,
            messageBuilder({typename: target, id: "7", value: "Hello from the future"}));
    }
});

/**
 * Showcase Part 3: Sending messages to egresses
 * =============================================
 * To let your StateFun application interact with the outside world, functions may write messages
 * to egresses. This function demonstrates sending messages to an Apache Kafka or AWS Kinesis
 * egress, which is currently our most commonly used egresses that are natively supported by
 * StateFun
 */
statefun.bind({
    typename: "showcase/egress",
    fn(context, message) {
        // there is a specific builder for messages to be sent to a Kafka egress ...
        context.send(
            kafkaEgressMessage({
                typename: "showcase/kafka-egress",
                topic: "my-kafka-topic",
                key: "my-key",
                value: "my-utf8-value"
            }));

        // and a builder for kinesis
        context.send(
            kinesisEgressMessage({
                typename: "showcase/kinesis-egress",
                stream: "a_stream",
                partitionKey: "akey", // can also be = context.self.id for example
                value: "aValue"
            }));

        // if you've implemented your own egress (it is currently only possible to do with Java)
        // then you can send it a message like this:
        context.send(egressMessageBuilder({
            target: "showecase/my-custom-egress",
            value: {"name": "Bob"},
            valueType: GreetJsonType
        }));
    }
});

/**
 * Showcase Part 4: Function state storage
 * =======================================
 *
 * One of StateFun's most powerful features is its capability of managing function state in a
 * consistent and fault-tolerant manner. This function demonstrates accessing and manipulating
 * persisted function state that is managed by StateFun.
 */

statefun.bind({
    typename: "showcase/storage",
    specs: [
        {
            name: "an_int",
            type: StateFun.intType(),
        },
        {
            name: "an_expiring_str",
            type: StateFun.stringType(),
            expireAfterWrite: 1_000
        },
        {
            name: "greet_json",
            type: GreetJsonType
        }
    ],

    fn(context, message) {
        /**
         * each function invocation gets access to storage that is scoped to the current function
         * instance's address, i.e. (function typename, instance id). For example, if (UserFn, "Gordon")
         * was invoked, the values you get access to belongs specifically to user Gordon.
         */
        const storage = context.storage

        // each value spec defined above, will appear as a property on the storage.
        console.log(storage.an_int)
        storage.an_int = 42

        // a value can be null if it has expired, or never set before.

        if (storage.an_expiring_str !== null) {
            console.log(storage.an_expiring_str)
        } else {
            console.log("Oh no, the str has expired (or wasn't set before)");
        }
        // a value can also be deleted by setting it explicitly to null.
        storage.greet_json = null;
    }
})

/**
 * Showcase Part 5: Asynchronous operations
 * ========================================
 * This function demonstrates performing asynchronous operations during a function invocation. It
 * is a common scenario for functions to have external dependencies in order for it to complete its
 * work, such as fetching enrichment information from remote databases.
 */
statefun.bind({
    typename: "showcase/async",
    async fn(context, message) {
        const name = nameSerivce(context.self.id);
        const favFood = foodService(context.self.id);

        const user = {name: await name, food: await favFood};

        context.egress(kafkaEgressMessage({
            typename: "showcase/kafka",
            topic: "enriched_users",
            key: context.self.id,
            value: JSON.stringify(user),
        }));
    }
});

/**
 *  Serving
 *  =======
 *  First, lets define a simple function that computes personalized greetings messages based on the number of times
 *  That this function was invoked.
 *  For demonstration purposes, this function prints to console the generated greetings
 *  messages.
 */
statefun.bind({
        typename: "showcase/serving",
        specs: [{name: "seen", type: StateFun.intType()}],
        fn(context, message) {
            let seen = context.storage.seen;
            seen += 1;
            context.storage.seen = seen;

            const greet = message.as(GreetJsonType);
            const name = greet.name;

            // in this example, the id part of the currently executing function
            // will be also equal to that name (context.self.id == name).
            console.log(`Hello ${name}! I've seen you ${seen} times!`);
        }
    }
);

/**
 Exposing the handler via HTTP
 =============================
 The code below handles the physical HTTP serving.
 In this case we chose aiohttp, although any other HTTP serving framework will do.
 The only thing that the HTTP handler needs to do, is to pass the raw (bytes) request body
 to StateFun's handler (defined above), and return the raw (bytes) StateFun's handler provided as a response.

 To actually start serving run from one terminal:
 node start

 and from another:
 docker-compose up

 edit input-example.json, try to add more json lines, then:
 docker-compose restart producer
 */

http.createServer(statefun.handler()).listen(8000);

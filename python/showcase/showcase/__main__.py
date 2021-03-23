################################################################################
#  Licensed to the Apache Software Foundation (ASF) under one
#  or more contributor license agreements.  See the NOTICE file
#  distributed with this work for additional information
#  regarding copyright ownership.  The ASF licenses this file
#  to you under the Apache License, Version 2.0 (the
#  "License"); you may not use this file except in compliance
#  with the License.  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
# limitations under the License.
################################################################################

import asyncio

from datetime import timedelta

from aiohttp import web
from statefun import *

from .showcase_custom_types_pb2 import UserProfile
from .showcase_custom_types import GREET_JSON_TYPE, USER_PROFILE_PROTOBUF_TYPE

functions = StatefulFunctions()


#  Showcase Part 1: Type System
#  ============================
#  This function demonstrates StateFun's type system using the Python SDK.
#
#  Core Type abstraction
#  =====================
#  The core abstraction used by StateFun's type system is the Type interface, which
#  consists of a few things that StateFun uses to handle messages and state values:
#
#  A TypeName to identify the type.
#  A TypeSerializer for serializing and deserializing instances of the type.
#
#  Cross-language primitive types
#  ==============================
#  StateFun's type system has cross-language support for common primitive types, such as boolean,
#  integer, long, etc. These primitive types have built-in Types implemented for them
#  already, with predefined typenames.
#
#  This is of course all transparent for the user, so you don't need to worry about it. Functions
#  implemented in various languages (e.g. Java or Python) can message each other by directly sending
#  supported primitive values as message arguments. Moreover, the type system is used for state
#  values as well; so, you can expect that a function can safely read previous state after
#  reimplementing it in a different language. We'll cover more on state storage access in later
#  parts of the showcase series.
#
#  Common custom types (e.g. JSON or Protobuf)
#  ===========================================
#  The type system is also very easily extensible to support custom message types, such as JSON
#  or Protobuf messages. This is just a matter of implementing your own Type with a custom
#  typename and serializer.
#
#  StateFun makes this super easy by providing builder utilities to help you create a simple
#  Type. Take a look at showcase_custom_types.py for few recommended ways to quickly create a StateFun Type
#  for your JSON or Protobuf messages.
#
@functions.bind("showcase/types")
def types(context, message: Message):
    # All values, including messages and storage values, are handled via StateFun's type system.
    # StateFun ships built-in primitive types that handles de-/serialization of messages across
    # functions:
    if message.is_bool():
        val = message.as_bool()
    elif message.is_int():
        # 32 bit, signed integer
        val = message.as_int()
    elif message.is_long():
        # 64 bit signed integer
        val = message.as_long()
    elif message.is_float():
        val = message.as_float()
    elif message.is_string():
        val = message.as_string()
    elif message.is_double():
        val = message.as_double()
    elif message.is_type(GREET_JSON_TYPE):
        # You can also define your own types using the type system, such as a JSON message ...
        val = message.as_type(GREET_JSON_TYPE)
    elif message.is_type(USER_PROFILE_PROTOBUF_TYPE):
        # Or Protobuf
        val = message.as_type(USER_PROFILE_PROTOBUF_TYPE)
    else:
        val = None

    print(f"I've got a message with {val} as payload!")


#
#  Showcase Part 2: Messaging Primitives
#  =====================================
#  This function demonstrates how to send messages to other functions.
#
@functions.bind("showcase/messaging")
def messaging(context: Context, message: Message):
    # You send messages to functions simply by specifying the target function's typename
    # and the target instance id for that for function; StateFun handles the routing for you,
    # without the need of any means for service discovery.
    target = "showcase/types"

    # you can directly send primitive type values as messages, ...
    context.send(message_builder(target_typename=target, target_id="0", bool_value=True))
    context.send(message_builder(target_typename=target, target_id="1", int_value=123))
    context.send(message_builder(target_typename=target, target_id="2", float_value=3.14159e+11))
    context.send(message_builder(target_typename=target, target_id="3", str_value="Hello world"))
    context.send(message_builder(target_typename=target, target_id="4", double_value=1.23))
    context.send(message_builder(target_typename=target, target_id="5", long_value=123456789))
    # ... or, in general, a value of any custom defined type.
    context.send(
        message_builder(target_typename=target, target_id="6", value={"name": "Joe"}, value_type=GREET_JSON_TYPE))

    # or extract the payload directly
    print(f"The payload's type is {message.value_typename()} and the raw bytes are {message.raw_value()}")

    # You can send messages to any function, including yourself!
    me = context.address
    context.send(message_builder(target_typename=me.typename, target_id=me.id, str_value="hello!"))

    # Additionally, you may ask StateFun to send out a message after a specified delay.
    # A common usage pattern is to send delayed messages to yourself to model timer triggers.
    context.send_after(timedelta(minutes=10),
                       message_builder(target_typename=target, target_id="7", str_value="Hello from the future"))


#  Showcase Part 3: Sending messages to egresses
#  =============================================
#  To let your StateFun application interact with the outside world, functions may write messages
#  to egresses. This function demonstrates sending messages to an Apache Kafka or AWS Kinesis
#  egress, which is currently our most commonly used egresses that are natively supported by
#  StateFun.
@functions.bind("showcase/egress")
def egress(context: Context, message: Message):
    # there is a specific builder for messages to be sent to a Kafka egress ...
    context.send_egress(
        kafka_egress_message(typename="showcase/kafka-egress", topic="my-kafka-topic", key="my-key",
                             value="my-utf8-value"))

    # and a builder for kinesis
    context.send(
        kinesis_egress_message(typename="showcase/kinesis-egress",
                               stream="a-stream",
                               partition_key="key",
                               explicit_hash_key="hash-key",
                               value="a value"))

    # if you've implemented your own egress (it is currently only possible to do with Java)
    # then you can send it a message like this:
    context.send_egress(egress_message_builder(target_typename="showecase/my-custom-egress",
                                               value={"name": "Bob"},
                                               value_type=GREET_JSON_TYPE))


#
#  Showcase Part 4: Function state storage
#  =======================================
#
#  One of StateFun's most powerful features is its capability of managing function state in a
#  consistent and fault-tolerant manner. This function demonstrates accessing and manipulating
#  persisted function state that is managed by StateFun.
#
@functions.bind("showcase/storage", specs=[
    ValueSpec(name="an_int", type=IntType),
    ValueSpec(name="an_expiring_str", type=StringType, expire_after_write=timedelta(days=7)),
    ValueSpec(name="greet_json", type=GREET_JSON_TYPE)
])
def storage(context, message):
    # each function invocation gets access to storage that is scoped to the current function
    # instance's address, i.e. (function typename, instance id). For example, if (UserFn, "Gordon")
    # was invoked, the values you get access to belongs specifically to user Gordon.
    storage = context.storage

    # each value spec defined above, will appear as a property on the storage.
    print(storage.an_int)
    storage.an_int = 42

    # a value can be None if it has expired, or never set before.
    if storage.an_expiring_str:
        print(storage.an_expiring_str)
    else:
        print("Oh no, the str has expiried (or wasn't set before)")

    # a value can also be deleted
    storage.greet_json = {"name": "Anton"}
    del storage.greet_json


#  Showcase Part 5: Asynchronous operations
#  ========================================
#  This function demonstrates performing asynchronous operations during a function invocation. It
#  is a common scenario for functions to have external dependencies in order for it to complete its
#  work, such as fetching enrichment information from remote databases.
@functions.bind("showcase/async", specs=[ValueSpec(name="user_profile", type=USER_PROFILE_PROTOBUF_TYPE)])
async def async_ops(context, message):
    profile = context.storage.user_profile
    if not profile:
        user_name = context.address.id

        profile = UserProfile()
        profile.name = user_name
        profile.favorite_ice_cream = await call_favorite_ice_cream_service(user_name)
        profile.favorite_tv_show = await call_faviorite_show_service(user_name)

        context.storage.user_profile = profile

    # reply to our caller with the computed user profile!
    caller: SdkAddress = context.caller
    context.send(message_builder(target_typename=caller.typename,
                                 target_id=caller.id,
                                 value=profile,
                                 value_type=USER_PROFILE_PROTOBUF_TYPE))


async def call_favorite_ice_cream_service(user_name: str) -> str:
    await asyncio.sleep(0.2)
    return "mint chocolate chip"


async def call_faviorite_show_service(user_name: str) -> str:
    await asyncio.sleep(0.5)
    return "The Office"


#  Serving
#  =======
#  1) First, lets define a simple function that computes personalized greetings messages based on the number of times
#  That this function was invoked.
#  For demonstration purposes, this function prints to console the generated greetings
#  messages.
@functions.bind("showcase/serving", specs=[ValueSpec(name="seen", type=IntType)])
async def serving(context, message: Message):
    seen = context.storage.seen or 0
    seen += 1
    context.storage.seen = seen

    greet_request = message.as_type(GREET_JSON_TYPE)
    name = greet_request["name"]

    # in this example, the id part of the currently executing function
    # will be also equal to that name (context.address.id == name).

    print(f"hello {name}! I've seen you {seen} times!", flush=True)


# Serving cont'
# ============
# create a request-reply handler for the registered functions, which understands how to
# decode invocation requests dispatched from StateFun cluster, dispatch to the correct function,
# and encode side-effects (e.g. storage updates, or invoking other functions)
# as responses to be handled by StateFun.

statefun_handler = RequestReplyHandler(functions)


# Exposing the handler via HTTP
# =============================
# The code below handles the physical HTTP serving.
# In this case we chose aiohttp, although any other HTTP serving framework will do.
# The only thing that the HTTP handler needs to do, is to pass the raw (bytes) request body
# to StateFun's handler (defined above), and return the raw (bytes) StateFun's handler provided as a response.
#
# To actually start serving run from one terminal:
#   python3 -m showcase
#
# and from another:
#   docker-compose up
#
# edit input-example.json, try to add more json lines, then:
# docker-compose restart producer

async def handle(request):
    req = await request.read()
    res = await statefun_handler.handle_async(req)
    return web.Response(body=res, content_type="application/octet-stream")


app = web.Application()
app.add_routes([web.post('/statefun', handle)])

if __name__ == '__main__':
    web.run_app(app, port=8000)

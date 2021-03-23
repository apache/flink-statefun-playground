# StateFun Python SDK Showcase

This project is intended for new StateFun users that would like to start implementing their StateFun application functions using Python.
The tutorial is streamlined and split into a few parts which we recommend to go through a specific order, as lay out below.
Each part is demonstrated with some code snippets plus comments to guide you through the SDK fundamentals.

## Prerequisites

- python3
- pip
- docker
- docker-compose

## Building the example

### Using venv

```
python3 -m venv venv
source venv/bin/activate
pip3 install .
```

## Tutorial Sections

The [__main__.py](showcase/__main__.py) file demonstrates SDK concepts at length, and it is highly recommended
to read through it. The sections below are copied from that file with some of the comments removed.


###  Type System
This function demonstrates StateFun's type system using the Python SDK.

```
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
        # Or protobuf
        val = message.as_type(USER_PROFILE_PROTOBUF_TYPE)
    else:
        val = None

    print(f"I've got a message with {val} as payload!")
```

###  Messaging Primitives

This function demonstrates how to send messages to other functions.

```
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

```

### Sending messages to egresses

To let your StateFun application interact with the outside world, functions may write messages
to egresses. This function demonstrates sending messages to an Apache Kafka or AWS Kinesis
egress, which is currently our most commonly used egresses that are natively supported by
StateFun.

```
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

```

###  Function state storage

```
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
```

###  Asynchronous operations

This function demonstrates performing asynchronous operations during a function invocation. It
is a common scenario for functions to have external dependencies in order for it to complete its
work, such as fetching enrichment information from remote databases.

```
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
```

###  Serving

* First, lets define a simple function that computes personalized greetings messages based on the number of times
that this function was invoked.
For demonstration purposes, this function prints to console the generated greetings
messages.

```
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
```

* Create a request-reply handler for the registered functions, which understands how to
decode invocation requests dispatched from StateFun cluster, dispatch to the correct function,
and encode side-effects (e.g. storage updates, or invoking other functions)
as responses to be handled by StateFun.

```
statefun_handler = RequestReplyHandler(functions)
```

* Exposing the handler via HTTP
The code below handles the physical HTTP serving.
In this case we chose `aiohttp`, although any other HTTP serving framework will do.
The only thing that the HTTP handler needs to do, is to pass the raw (bytes) request body
to StateFun's handler (defined above), and return the raw (bytes) StateFun's handler provided as a response.

```
async def handle(request):
    req = await request.read()
    res = await statefun_handler.handle_async(req)
    return web.Response(body=res, content_type="application/octet-stream")


app = web.Application()
app.add_routes([web.post('/statefun', handle)])

if __name__ == '__main__':
    web.run_app(app, port=8000)
```

To actually start serving run from one terminal:
```python3 -m showcase```

And from another:
```docker-compose up```

# Next Steps

The setup you executed in the last part of this tutorial is not how you'd normally deploy StateFun processes
and functions. It's a rather simplified setup to allow you to explore the interaction between
functions and the StateFun processes by setting debugger breakpoints in the function code in your IDE.

We recommend now to take a look at a slightly more realistic setup, using Docker Compose, in the
[Greeter Docker Compose Example](../greeter).

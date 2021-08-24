# StateFun GoLang SDK Showcase

This project is intended for new StateFun users that would like to start implementing their StateFun application functions using GoLang.
The tutorial is streamlined and split into a few parts which we recommend to go through a specific order, as lay out below.
Each part is demonstrated with some code snippets plus comments to guide you through the SDK fundamentals.

## Prerequisites

- golang
- docker-compose

## Tutorial Sections

###  Type System

This function demonstrates StateFun's type system using the GoLang SDK.


###  Messaging Primitives

[This function](pkg/showcase/part1/types.go) demonstrates how to send messages to other functions.

### Sending messages to egresses

To let your StateFun application interact with the outside world, functions may write messages
to egresses. [This function](pkg/showcase/part3/egress.go) demonstrates sending messages to an Apache Kafka or AWS Kinesis
egress, which is currently our most commonly used egresses that are natively supported by
StateFun.

###  Function state storage

Consistent state is at the core of stateful functions. [This function](pkg/showcase/part4/storage.go)
demonstrates interacting with function state. 

###  Asynchronous operations

[This function](pkg/showcase/part5/asyncops.go) demonstrates performing asynchronous operations during a function invocation. It
is a common scenario for functions to have external dependencies in order for it to complete its
work, such as fetching enrichment information from remote databases.

###  Serving

[This function](pkg/showcase/part6/serving.go) builds a full stateful functions application
and shows how they are exposed and deployed in the real world. Run this function locally 
along with the stateful functions runtime!

To actually start serving run from one terminal:
```bash
$ cd pkg/showcase/part6
$ go build
$ ./part6
```

And from another:
```docker-compose up```

# Next Steps

The setup you executed in the last part of this tutorial is not how you'd normally deploy StateFun processes
and functions. It's a rather simplified setup to allow you to explore the interaction between
functions and the StateFun processes by setting debugger breakpoints in the function code in your IDE.

We recommend now to take a look at a slightly more realistic setup, using Docker Compose, in the
[Greeter Docker Compose Example](../greeter).

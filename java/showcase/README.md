# StateFun Java SDK Showcase

This Java project is intended for new StateFun users that would like to start implementing their StateFun application functions using Java (or any other JVM language)!

The tutorial is streamlined and split into a few parts which we recommend to go through a specific order, as lay out below.
Each part is demonstrated with some code snippets plus Javadocs and comments to guide you through the SDK fundamentals.
We highly recommend importing this project into an IDE (we suggest IntelliJ IDEA) to ease navigation through the classes and pointers throughout the tutorial.

To import this project to your IDE, you should directly import it as a Maven project using the [POM](pom.xml) file.

## Prerequisites

- Java 8
- Maven 3.5+
- IntelliJ IDEA
- Docker 20.10+

## Tutorial Sections

### Part 1: [Type system](src/main/java/org/apache/flink/statefun/playground/java/showcase/part1/types/TypeSystemShowcaseFn.java)
This starter section will guide you through StateFun's universal cross-language type system used for messages and state storage.

### Part 2: [Messaging primitives](src/main/java/org/apache/flink/statefun/playground/java/showcase/part2/messaging/MessagingPrimitivesShowcaseFn.java)
Learn about how to use the SDK to send messages from one function to another!

### Part 3: [Egresses](src/main/java/org/apache/flink/statefun/playground/java/showcase/part3/egresses/EgressShowcaseFn.java)
See how functions can interact with the outside world external to your StateFun application using egresses.

### Part 4: [State storage](src/main/java/org/apache/flink/statefun/playground/java/showcase/part4/storage/StateStorageShowcaseFn.java)
One of StateFun's most powerful features is its capability of managing function state in a consistent and fault-tolerant
manner. This part of the tutorial covers how to access managed state storage using the SDK.

### Part 5: [Asynchronous operations](src/main/java/org/apache/flink/statefun/playground/java/showcase/part5/asyncops/AsyncOpsShowcaseFn.java)
This section demonstrates performing asynchronous operations during a function invocation. It is a common scenario for
functions to have external dependencies in order for it to complete its work, such as fetching enrichment information
from remote databases.

### Part 6: [Serving functions](src/main/java/org/apache/flink/statefun/playground/java/showcase/part6/serving/GreeterAppServer.java)
The grand finale of this tutorial series. In this last part of the tutorial, you will serve functions via a HTTP web
server, so that it is reachable by the StateFun runtime. This final part of the tutorial provides a runnable demo
experience, so we highly recommend taking a look to see everything in action!

## Next Steps

The setup you executed in the last part of this tutorial is not how you'd normally deploy StateFun processes
and functions. It's a rather simplified setup to allow you to explore the interaction between
functions and the StateFun processes by setting debugger breakpoints in the function code in your IDE.

We recommend now to take a look at a slightly more realistic setup, using Docker Compose, in the
[Greeter Docker Compose Example](../greeter).

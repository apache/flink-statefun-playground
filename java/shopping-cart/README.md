# Shopping Cart Example with Docker Compose

This example demonstrates interaction between two stateful functions - one responsible for managing the users' shopping carts (`UserShoppingCartFn`), and the other responsible for managing the stock (`StockFn`). It is intended to showcase a somewhat more complex business logic where consistent state guarantees span multiple interacting stateful functions. You can think about them as two microservices that 'magically' always stay in consistent state with respect to each other and the output, without having to synchronize them or reconciliate their state in case of failures. This example uses an egress in exactly-once mode. This means that the receipt is produced to the output only if the internal fault-tolerate state of the functions got consistently updated according to the checkout request (requires `read_committed` consumer isolation level).  

If you are new to stateful functions, we recommend you to first look at a more simple example, the [Greeter Example](../greeter).

## Directory structure

- `src/`, `pom.xml` and `Dockerfile`: These files and directories are the contents of a Java Maven project which builds
  our functions service, hosting the `UserShoppingCartFn` and `StockFn` behind a HTTP endpoint. Check out the source code under
  `src/main/java`. The `Dockerfile` is used to build a Docker image for our functions service.
- `module.yaml`: The [Module Specification]() file to be mounted to the StateFun runtime process containers. This
  configures a few things for a StateFun application, such as the service endpoints of the application's functions, as
  well as definitions of [Ingresses and Egresses]() which the application will use.
- `docker-compose.yml`: Docker Compose file to spin up everything.
- `playthrough`: utilities for automatically playing through the interactions scenarios.

## Prerequisites

- Docker
- Docker Compose

## Running the example

This example works with Docker Compose, and runs a few services that build up an end-to-end StateFun application:
- Functions service that runs your functions and expose them through an HTTP endpoint.
- StateFun runtime processes (a manager plus workers) that will handle ingress, egress, and inter-function messages as
  well as function state storage in a consistent and fault-tolerant manner.
- Apache Kafka broker for the application ingress and egress. StateFun currently natively supports AWS Kinesis as well,
  and you can also extend to connect with other systems.

To build the example, execute:

```
cd java/shopping-cart
docker-compose build
```

This pulls all the necessary Docker images (StateFun and Kafka), and also builds the functions service image. This can
take a few minutes as it also needs to build the function's Java project.

Afterward the build completes, start running all the services:

```
docker-compose up
```

## Play around!

The `playground` folder contains scenario(s) and utilities which allow you to easily execute a set of steps that emulate interactions with the stateful functions.

In order to run a scenario, execute:
```
cd java/shopping-cart/playthrough
./scenario_1.sh
```

It will send a series of messages, results of which you can observe in the logs of the `shopping-cart-functions` component:
```
docker-compose logs -f shopping-cart-functions
```
Note: `Caller: Optional.empty` in the logs corresponds to the messages that came via an ingress rather than from another stateful function. 

To see the results produced to the egress:
```
docker-compose exec kafka bash -c '/usr/bin/kafka-console-consumer --topic receipts --bootstrap-server kafka:9092'
```

If you want to modify the code, you can do a hot redeploy of your functions service:
```
docker-compose up -d --build shopping-cart-functions
```
This rebuilds the functions service image with the updated code, and restarts the service with the new image.




# Shopping Cart Example with Docker Compose

This example demonstrates interaction between two stateful functions - one responsible for managing the users' shopping carts (`UserShoppingCartFn`), and the other responsible for managing the stock (`StockFn`). It is intended to showcase a somewhat more complex business logic where consistent state guarantees span multiple interacting stateful functions. You can think about them as two microservices that 'magically' always stay in consistent state with respect to each other and the output, without having to synchronize them or reconciliate their state in case of failures. This example uses an egress in exactly-once mode. This means that the receipt is produced to the output only if the internal fault-tolerate state of the functions got consistently updated according to the checkout request (requires `read_committed` consumer isolation level).  

If you are new to stateful functions, we recommend you to first look at a more simple example, the [Greeter Example](../greeter).

## Directory structure

- `src/`, `pom.xml` and `Dockerfile`: These files and directories are the contents of a Java Maven project which builds
  our functions service, hosting the `UserShoppingCartFn` and `StockFn` behind a HTTP endpoint. Check out the source code under
  `src/main/java`. The `Dockerfile` is used to build a Docker image for our functions service.
- `module.yaml`: The [Module Specification](https://ci.apache.org/projects/flink/flink-statefun-docs-release-3.2/docs/deployment/module/) file to be mounted to the StateFun runtime process containers. This
  configures a few things for a StateFun application, such as the service endpoints of the application's functions, as
  well as definitions of [Ingresses and Egresses](https://ci.apache.org/projects/flink/flink-statefun-docs-release-3.2/docs/io-module/overview/) which the application will use.
- `docker-compose.yml`: Docker Compose file to spin up everything.

## Prerequisites

- Docker
- Docker Compose

## Running the example

This example works with Docker Compose, and runs a few services that build up an end-to-end StateFun application:
- Functions service that runs your functions and expose them through an HTTP endpoint.
- StateFun runtime processes (a manager plus workers) that will handle ingress, egress, and inter-function messages as
  well as function state storage in a consistent and fault-tolerant manner.

To build the example, execute:

```
cd java/shopping-cart
docker-compose build
```

This pulls all the necessary Docker images (StateFun), and also builds the functions service image. This can
take a few minutes as it also needs to build the function's Java project.

Afterward the build completes, start running all the services:

```
docker-compose up
```

## Play around!

The shopping cart examples allows you to do the following actions:

* Stock items up via sending a `RestockItem` message to the `stock` function
* Add items to a cart via sending a `AddToCart` message to the `user-shopping-cart` function
* Checkout the cart via sending a `Checkout` message to the `user-shopping-cart` function
* Clear the cart via sending a `ClearCart` message to the `user-shopping-cart` function

### Example scenario

The example scenario adds a socks item to the stock.

```
$ curl -X PUT -H "Content-Type: application/vnd.com.example/RestockItem" -d '{"itemId": "socks", "quantity": 50}' localhost:8090/com.example/stock/socks
```

Then we add this item to a user cart and check it out.

```
$ curl -X PUT -H "Content-Type: application/vnd.com.example/AddToCart" -d '{"userId": "1", "quantity": 3, "itemId": "socks"}' localhost:8090/com.example/user-shopping-cart/1
$ curl -X PUT -H "Content-Type: application/vnd.com.example/Checkout" -d '{"userId": "1"}' localhost:8090/com.example/user-shopping-cart/1
```

The receipt can then be observed by reading from the egress.

```
$ curl -X GET localhost:8091/receipts
```

The scenario will send a series of messages, results of which you can observe in the logs of the `shopping-cart-functions` component:
```
docker-compose logs -f shopping-cart-functions
```
Note: `Caller: Optional.empty` in the logs corresponds to the messages that came via an ingress rather than from another stateful function.

### Messages

The messages are expected to be encoded as JSON.

* `RestockItem`: `{"itemId": "socks", "quantity": 50}`, `itemId` is the id of the `stock` function
* `AddToCart`: `{"userId": "1", "quantity": 3, "itemId": "socks"}`, `userId` is the id of the `user-shopping-cart` function
* `Checkout`: `{"userId": "1"}`, `userId` is the id of the `user-shopping-cart` function
* `ClearCart`: `{"userId": "1"}`, `userId` is the id of the `user-shopping-cart` function

## What's next?

If you want to modify the code, you can do a hot redeploy of your functions service:
```
docker-compose up -d --build shopping-cart-functions
```
This rebuilds the functions service image with the updated code, and restarts the service with the new image.

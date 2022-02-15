# Connected Components Example with Docker Compose

This example is intended as a follow-up after completion of the [Java SDK Showcase Tutorial](../showcase). If you're
already familiar with the Java SDK fundamentals and would like to get a better understanding of how a realistic StateFun
application looks like, then you're in the right place! Otherwise, we highly suggest taking a look at the Showcase
tutorial first.

This example works with Docker Compose, and runs a few services that build up an end-to-end StateFun application:
- Functions service that runs your functions and expose them through an HTTP endpoint.
- StateFun runtime processes (a manager plus workers) that will handle ingress, egress, and inter-function messages as
  well as function state storage in a consistent and fault-tolerant manner.

To motivate this example, we'll implement a [connected components](https://en.wikipedia.org/wiki/Component_(graph_theory) algorithm on top of Stateful Functions.
The program has one function - a `ConnectedComponentsFn` that consumes `Vertex` JSON events from an ingress and communicates with its neighbours to find the minimal component id.
Changes of the component id of a vertex are being output via an egress.

## Directory structure

- `src/`, `pom.xml` and `Dockerfile`: These files and directories are the contents of a Java Maven project which builds
  our functions service, hosting the `ConnectedComponentsFn` behind a HTTP endpoint. Check out the source code under
  `src/main/java`. The `Dockerfile` is used to build a Docker image for our functions service.
- `module.yaml`: The [Module Specification](https://ci.apache.org/projects/flink/flink-statefun-docs-release-3.0/docs/deployment/module/) file to be mounted to the StateFun runtime process containers. This
  configures a few things for a StateFun application, such as the service endpoints of the application's functions, as
  well as definitions of [Ingresses and Egresses](https://ci.apache.org/projects/flink/flink-statefun-docs-release-3.0/docs/io-module/overview/) which the application will use.
- `docker-compose.yml`: Docker Compose file to spin up everything.

## Prerequisites

- Docker
- Docker Compose

## Running the example

First, lets build the example. From this directory, execute:

```
$ docker-compose build
```

This pulls all the necessary Docker images (StateFun), and also builds the functions service image. This can
take a few minutes as it also needs to build the function's Java project.

Afterward the build completes, start running all the services:

```
$ docker-compose up
```

## Play around!

The connected components applications allows you to do the following actions:

* Add a new vertex to the graph via sending a `Vertex` message to the `vertex` function

In order to send messages to the Stateful Functions application you can run:

```
$ curl -X PUT -H "Content-Type: application/vnd.connected-components.types/vertex" -d '{"vertex_id": "1", "neighbours": ["2", "3"]}' localhost:8090/connected-components.fns/vertex/1
$ curl -X PUT -H "Content-Type: application/vnd.connected-components.types/vertex" -d '{"vertex_id": "2", "neighbours": ["1", "4"]}' localhost:8090/connected-components.fns/vertex/2
$ curl -X PUT -H "Content-Type: application/vnd.connected-components.types/vertex" -d '{"vertex_id": "3", "neighbours": ["1"]}' localhost:8090/connected-components.fns/vertex/3
$ curl -X PUT -H "Content-Type: application/vnd.connected-components.types/vertex" -d '{"vertex_id": "4", "neighbours": ["2"]}' localhost:8090/connected-components.fns/vertex/4
```

You can take a look at what messages are being sent to the Playground egress:

```
$ curl -X GET localhost:8091/connected-component-changes
```

### Messages

All messages are expected to be encoded as JSON:

* `Vertex`: `{"vertex_id": "1", "neighbours": ["2", "3"]}`, `vertex_id` is the id of the `vertex` function

## What's next?

You can also try modifying the function code in the `src/main/java` directory, and do a zero-downtime upgrade of the
functions. Some ideas you can try out:
- Enable the connected component computation for graphs with undirected edges
- Make the neighbour set changeable

After you've finished changing the function code, you can do a hot redeploy of your functions service:

```
$ docker-compose up -d --build connected-components-functions
```

This rebuilds the functions service image with the updated code, and restarts the service with the new image.

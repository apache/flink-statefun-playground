# Kubernetes Example

## Background

This example demonstrates deploying `Apache Flink StateFun`, together with a single [remote function](03-functions/functions.py) to `kubernetes`.

All the components are already pre-configured and contains all the resource definitions ready to be applied with `kubectl`.
All the resources are deployed to a dedicated namespace - `statefun`.

## What's inside?

The following components will be installed:

1. Apache Kafka
2. MinIO (S3 compatible object store)
3. A StateFun service [functions.py](03-functions/functions.py)
4. Apache Flink Cluster that executes a StateFun Job.  Take a look at [module.yaml](04-statefun/00-module.yaml) config-map. 
	The resources defined at [statefun-runtime.yaml](04-statefun/ 01-statefun-runtime.yaml) are already pre-configured for that specific example.

## Prerequisites


```
minikube config set memory 5120
minikube start
minikube ssh 'sudo ip link set docker0 promisc on'
eval $(minikube -p minikube docker-env)
```

## Create the `statefun` namespace.

```
kubectl create -f 00-namespace
kubectl config set-context --current --namespace statefun
```

## Create auxiliary services that are need for this example:
 
```
kubectl create -f 01-minio
kubectl create -f 02-kafka
```

## Create the function:

**NOTE:**  please make sure that you've run `eval $(minikube -p minikube docker-env)` In your current terminal session.

```
cd 03-functions
make image
make service
cd ..
```

## Start the StateFun runtime

```
kubectl create -f 04-statefun
```

## Open the Flink's WEB UI

```
 kubectl port-forward svc/statefun-master-rest 8081:8081 -n statefun
```

Now you can explore Apache Flink's WEB interface:

[http://localhost:8081/#/overview](http://localhost:8081/#/overview)

## Interact with the Function!

In one terminal run:
```
bin/show-function-logs.sh
```

And in another run:

```
bin/invoke-function.sh
```

When prompted with:
```
Please enter a target id:
```

try writing your name.

```
Please enter a message:
```

Write any message you would like.

In the previous console you will see:
`Hello from <name>: you wrote  <message>!`

## Teardown

```
kubectl delete namespace statefun
minikube stop
```




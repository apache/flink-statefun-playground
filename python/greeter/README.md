# The Greeter Example

This is a simple example of a stateful functions application implemented in `Python`.

In this example, we imagine a service that computes personalized greetings. 
Our service, consist out of the following components:

* `kafka ingress` - This component forwards messages produced to the `names` kafka topic,
to the `person` stateful function. Messages produced to this topic has the following 
schema `{ "name" : "bob"}`.

* `person` - This function is triggered by the ingress defined above.
This function keeps track of the number of visits, and triggers the next functions:

* `greeter` - This function, computes a personalized greeting, based on the name and the number
of visits of that user. The output of that computation is forward to a Kafka egress defined below.
 
* `kafka egress` - This wraps a Kafka producer that emits `utf-8` greetings to the `greetings` Kafka topic.


![Flow](arch.png "Flow")

## Running the example

```
docker-compose build
docker-compose up
```

To observe the customized greeting, as they appear in the `greetings` Kafka topic, run in a separate terminal:

```
docker-compose exec kafka kafka-console-consumer \
     --bootstrap-server kafka:9092 \
     --isolation-level read_committed \
     --from-beginning \
     --topic greetings
```

Try adding few more input lines to [input-example.json](input-example.json), and restart
the producer service.

```
docker-compose restart producer
``` 

Feeling curious? add the following print to the `person` function at [functions.py](functions.py):
```print(f"Hello there {context.address.id}!", flush=True)```.

Then, rebuild and restart only the `functions` service.

```
docker-compose build functions
docker-compose up functions
```

# Kafka Producer

# What
This is a simple, single threaded `Python` script that produces JSONs into a `Kafka` topic.
Since in StateFun world, we also require keys (they are used for the initial routing) a JSON path
string needs to be provided. 

# Environment Variables

(taken directly from `main.py`)

```
APP_PATH = Arg(key="APP_PATH", default="/opt/statefun/example.json", type=str)
APP_KAFKA_HOST = Arg(key="APP_KAFKA_HOST", default="kafka-broker:9092", type=str)
APP_KAFKA_TOPIC = Arg(key="APP_KAFKA_TOPIC", default="input", type=str)
APP_DELAY_SECONDS = Arg(key="APP_DELAY_SECONDS", default="1", type=int)
APP_LOOP = Arg(key="APP_LOOP", default="true", type=lambda s: s.lower() == "true")
APP_JSON_PATH = Arg(key="APP_JSON_PATH", default="name", type=parse)
```

# Example:

Imagine we have the following file at `/opt/statefun/example.json`

```
> cat /opt/statefun/example.json
{"name" : "Gorge", "last" : "Costanza"}
{"name" : "Bob", "last" : "Mop"}
```

Then we need to set:

```
export APP_PATH=/opt/statefun/example.json
export APP_JSON_PATH='name' # note there is no leading dot.
```


# Docker

* Build via: `docker build . -t app`
* Run `docker run -v $(pwd):/mnt -e 'APP_PATH=/mnt/example.json' -it app`

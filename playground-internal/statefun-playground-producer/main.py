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
import json
import signal
import sys
import time
import os
from collections import namedtuple
from itertools import cycle

from kafka.errors import NoBrokersAvailable
from kafka import KafkaProducer
from jsonpath_ng import parse

Arg = namedtuple('Arg', ['key', 'default', 'type'])

APP_PATH = Arg(key="APP_PATH", default="/opt/statefun/example.json", type=str)
APP_KAFKA_HOST = Arg(key="APP_KAFKA_HOST", default="kafka-broker:9092", type=str)
APP_KAFKA_TOPIC = Arg(key="APP_KAFKA_TOPIC", default="input", type=str)
APP_DELAY_SECONDS = Arg(key="APP_DELAY_SECONDS", default="1", type=int)
APP_LOOP = Arg(key="APP_LOOP", default="true", type=lambda s: s.lower() == "true")
APP_JSON_PATH = Arg(key="APP_JSON_PATH", default="name", type=parse)


def env(arg: Arg):
    val = os.environ.get(arg.key, arg.default)
    return arg.type(val)


def read_jsons(path: str):
    with open(path) as f:
        for line in f.read().splitlines():
            yield json.loads(line)


def create_requests(path: str, loop: bool, json_path):
    jsons = [js for js in read_jsons(path)]
    if loop:
        jsons = cycle(jsons)
    for js in jsons:
        matches = json_path.find(js)
        if len(matches) != 1:
            raise ValueError(f"Unable to find exactly one key at {js}, please check the correctness of your {APP_JSON_PATH.key} value")
        match = matches[0]
        yield match.value, js


class KProducer(object):

    def __init__(self, broker, topic):
        self.producer = KafkaProducer(bootstrap_servers=[broker])
        self.topic = topic

    def send(self, key: str, value: str):
        key = key.encode('utf-8')
        value = value.encode('utf-8')
        self.producer.send(topic=self.topic, key=key, value=value)
        self.producer.flush()


def produce(producer, delay_seconds: int, requests):
    for key, js in requests:
        value = json.dumps(js)
        producer.send(key=key, value=value)
        if delay_seconds > 0:
            time.sleep(delay_seconds)


def handler(number, frame):
    sys.exit(0)


def main():
    # setup an exit signal handler
    signal.signal(signal.SIGTERM, handler)
    signal.signal(signal.SIGINT, handler)
    # get the key,value request generators
    requests = create_requests(path=env(APP_PATH), loop=env(APP_LOOP), json_path=env(APP_JSON_PATH))
    # produce forever
    while True:
        try:
            producer = KProducer(broker=env(APP_KAFKA_HOST), topic=env(APP_KAFKA_TOPIC))
            produce(producer=producer, delay_seconds=env(APP_DELAY_SECONDS), requests=requests)
            print("Done producing, good bye!", flush=True)
            return
        except SystemExit:
            print("Good bye!", flush=True)
            return
        except NoBrokersAvailable:
            print("No brokers available... retrying in 2 seconds.")
            time.sleep(2)
            continue
        except Exception as e:
            print(e)
            return


if __name__ == "__main__":
    main()

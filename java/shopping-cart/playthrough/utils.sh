#!/bin/bash

# Sends messages to Kafka within docker-compose setup.
# Parameters:
#  - param1: message key
#  - param2: message payload
#  - param3: Kafka topic
send_to_kafka () {
    local key=$1
    local payload=$2
    local topic=$3
    echo "Sending \"$payload\" with key \"$key\" to \"$topic\" topic"
    docker-compose exec kafka bash -c "echo '$key: $payload' | /usr/bin/kafka-console-producer --topic $topic --broker-list kafka:9092 --property 'parse.key=true' --property 'key.separator=:'"
}
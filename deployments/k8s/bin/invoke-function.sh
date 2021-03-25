#!/bin/bash

POD="$(kubectl get pods -n statefun -l app=kafka --no-headers=true -o custom-columns=:metadata.name)"

send_to_kafka () {
    local key=$1
    local payload=$2
    kubectl exec -n statefun -it ${POD} -- bash -c "unset JMX_PORT && unset KAFKA_JMX_OPTS && echo '$key: $payload' | kafka-console-producer.sh --topic names --broker-list localhost:9092 --property 'parse.key=true' --property 'key.separator=:'"
}

if [ -z "$POD" ]; then
	echo "Unable to find a Kafka broker. Did you run: kubectl create -f kafka.yaml ?"
	exit 1
fi

echo "Kafka POD found at ${POD}"
echo "hit CTRL+C to exit ..."

while [ 1 ]; do
	echo "Please enter a target id:"
	read key
	if [ -z "$key" ]; then
		echo "target must be provided."
		continue
	fi

	echo "Please enter a message"
	read value
	send_to_kafka "$key" "$value" &> /dev/null
done

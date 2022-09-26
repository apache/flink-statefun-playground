package org.apache.flink;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.utility.DockerImageName;

public class ProducerRuntimeProcess {
    private static final Logger LOG =
            LoggerFactory.getLogger(ProducerRuntimeProcess.class);

    private static final Network NETWORK = Network.newNetwork();

//    private static final GenericContainer<?> KAFKA_JSON_PRODUCER_ONE =
//            ProducerOneContainer(NETWORK).dependsOn(StatefulFunctionsRuntimeProcesses.STATEFUN_WORKER, StatefulFunctionsRuntimeProcesses.KAFKA);
//
//    private static final GenericContainer<?> KAFKA_JSON_PRODUCER_TWO =
//            ProducerTwoContainer(NETWORK).dependsOn(StatefulFunctionsRuntimeProcesses.STATEFUN_WORKER, StatefulFunctionsRuntimeProcesses.KAFKA);

    private static final GenericContainer<?> KAFKA_JSON_PRODUCER_ONE =
            ProducerOneContainer(NETWORK);
    private static final GenericContainer<?> KAFKA_JSON_PRODUCER_TWO =
            ProducerTwoContainer(NETWORK);

    public static void main(String[] args) throws Exception {
        try {
            KAFKA_JSON_PRODUCER_ONE.start();
            KAFKA_JSON_PRODUCER_TWO.start();

            sleep();
        } finally {
            KAFKA_JSON_PRODUCER_ONE.stop();
            KAFKA_JSON_PRODUCER_TWO.stop();
        }
    }

    private static GenericContainer<?> ProducerOneContainer(Network network) {
        return new GenericContainer<>(
                DockerImageName.parse("ververica/statefun-playground-producer:latest"))
                .withNetwork(network)
                .withClasspathResourceMapping(
                        "text1.txt", "/opt/statefun/text1.txt", BindMode.READ_ONLY)
                .withEnv("APP_PATH", "/opt/statefun/text1.txt")
                .withEnv("APP_KAFKA_HOST", "kafka:9092")
                .withEnv("APP_KAFKA_TOPIC", "word-frequency")
                .withEnv("APP_JSON_PATH", "text");
    }

    private static GenericContainer<?> ProducerTwoContainer(Network network) {
        return new GenericContainer<>(
                DockerImageName.parse("ververica/statefun-playground-producer:latest"))
                .withNetwork(network)
                .withClasspathResourceMapping(
                        "text2.txt", "/opt/statefun/text2.txt", BindMode.READ_ONLY)
                .withEnv("APP_PATH", "/opt/statefun/text2.txt")
                .withEnv("APP_KAFKA_HOST", "kafka:9092")
                .withEnv("APP_KAFKA_TOPIC", "word-frequency")
                .withEnv("APP_JSON_PATH", "text");
    }

    private static void sleep() throws Exception {
        while (true) {
            Thread.sleep(10000);
        }
    }
}

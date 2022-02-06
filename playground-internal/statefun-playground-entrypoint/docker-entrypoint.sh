#!/usr/bin/env bash

java \
-XX:MaxDirectMemorySize=${MAX_DIRECT_MEMORY_SIZE:-80m} \
-XX:MaxMetaspaceSize=${MAX_METASPACE_SIZE:-64m} \
-Xms${MIN_HEAP_SIZE:-64m} \
-Xmx${MAX_HEAP_SIZE:-192m} \
-jar \
statefun-playground-entrypoint.jar \
"$@"

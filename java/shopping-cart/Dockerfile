# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# Build the functions code ...
FROM maven:3.6.3-jdk-11 AS builder
COPY pom.xml /usr/src/app/
# Build dependencies and cache this layer
RUN mvn -f /usr/src/app dependency:go-offline package -B
COPY src /usr/src/app/src
RUN mvn -f /usr/src/app/pom.xml clean package

# ... and run the web server!
FROM openjdk:8
WORKDIR /
COPY --from=builder /usr/src/app/target/shopping-cart*jar-with-dependencies.jar shopping-cart.jar
EXPOSE 1108
CMD java -jar shopping-cart.jar

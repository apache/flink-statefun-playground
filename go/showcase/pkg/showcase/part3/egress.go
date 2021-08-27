// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package part3

import "github.com/apache/flink-statefun/statefun-sdk-go/v3/pkg/statefun"

// EgressFn
//   Showcase Part 3: Sending messages to egresses
//
//   ============================
//
//   To let your StateFun application interact with the outside world, functions may write messages
//   to egresses. This function demonstrates sending messages to an Apache Kafka or AWS Kinesis
//   egress, which is currently our most commonly used egresses that are natively supported by StateFun.
//
//   ============================
//
//   Next, we recommend learning about StateFun's state storage management and how to access stored
//   values. Head over to StateStorageFn.
func EgressFn(ctx statefun.Context, _ statefun.Message) error {
	ctx.SendEgress(statefun.KafkaEgressBuilder{
		Target: KafkaEgressTypeName,
		Topic:  "my-kafka-topic",
		Key:    "my-key",
		Value:  []byte("hello world!"),
	})

	ctx.SendEgress(statefun.KinesisEgressBuilder{
		Target:          KinesisEgressTypeName,
		Stream:          "my-kinesis-stream",
		Value:           []byte("hello world again!"),
		PartitionKey:    "my-partition-key",
		ExplicitHashKey: "my-explicit-hash-key",
	})

	return nil
}

// Egresses are associated with a unique TypeName. The typename is used to identify which
// egress a message should be sent to.
//
// StateFun currently has native support for using Apache Kafka topics or AWS Kinesis streams
// as egresses.
//
// Registering a Kafka or Kinesis egress under a given TypeName is beyond the scope of
// what this specific part of the tutorial is attempting to demonstrate. For the time being,
// you can simply assume that a Kafka egress has been registered under the typename
// literal `golang.showcase.io/my-kafka-egress` and a Kinesis egress has been registered
// under the typename literal `golang.showcase.io/my-kinesis-egress`.
var (
	KafkaEgressTypeName   = statefun.TypeNameFrom("showcase.io/my-kafka-egress")
	KinesisEgressTypeName = statefun.TypeNameFrom("showcase.io/my-kinesis-egress")
)

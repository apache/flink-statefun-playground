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

package main

import (
	"fmt"
	"github.com/apache/flink-statefun/statefun-sdk-go/v3/pkg/statefun"
	"log"
)

var (
	GreetingTypeName = statefun.TypeNameFrom("showcase.fns/greetings")
	KafkaEgress      = statefun.TypeNameFrom("showcase.io/greets")
	templates        = []string{"Welcome %s!", "Nice to see you again %s.", "Third time is a charm %s!"}
)

// greetings is a simple function that computes personalized
// greeting messages based on a give UserProfile. Then, it sends
// the greeting back to the user via an egress Kafka topic.
//
// For demonstration purposes, this function also prints to the
// console the generated greeting messages.
func greetings(ctx statefun.Context, message statefun.Message) error {
	var profile UserProfile
	_ = message.As(UserProfileType, &profile)

	greeting := computeGreeting(profile)
	log.Printf("GreeingsFn (instance id: %s):\t%s", ctx.Self().Id, greeting)

	ctx.SendEgress(statefun.KafkaEgressBuilder{
		Target: KafkaEgress,
		Topic:  "greetings",
		Key:    ctx.Self().Id,
		Value:  []byte(greeting),
	})

	return nil
}

func computeGreeting(profile UserProfile) string {
	count := profile.SeenCount

	if count <= int32(len(templates)) {
		return fmt.Sprintf(templates[count-1], profile.Name)
	}

	return fmt.Sprintf("Nice to see you for the %dth time, %s! It has been %d milliseconds since we last say you.",
		count, profile.Name, profile.LastSeenDeltaMs)
}

func GreetingSpec() statefun.StatefulFunctionSpec {
	return statefun.StatefulFunctionSpec{
		FunctionType: GreetingTypeName,
		Function:     statefun.StatefulFunctionPointer(greetings),
	}
}

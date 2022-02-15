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
	"net/http"
)

type GreetRequest struct {
	Name   string `json:"name"`
	Visits int32  `json:"visits"`
}

type EgressRecord struct {
	Topic string `json:"topic"`
	Payload string `json:"payload"`
}

var (
	PersonTypeName      = statefun.TypeNameFrom("example/person")
	GreeterTypeName     = statefun.TypeNameFrom("example/greeter")
	PlaygroundEgressTypeName = statefun.TypeNameFrom("io.statefun.playground/egress")
	GreetRequestType    = statefun.MakeJsonType(statefun.TypeNameFrom("example/GreetRequest"))
	EgressRecordType    = statefun.MakeJsonType(statefun.TypeNameFrom("io.statefun.playground/EgressRecord"))
)

type Person struct {
	Visits statefun.ValueSpec
}

func (p *Person) Invoke(ctx statefun.Context, message statefun.Message) error {
	// update the visit count.
	var visits int32
	ctx.Storage().Get(p.Visits, &visits)

	visits += 1

	fmt.Printf("seen %d", visits)
	ctx.Storage().Set(p.Visits, visits)

	// enrich the request with the number of visits.
	var request GreetRequest
	if err := message.As(GreetRequestType, &request); err != nil {
		return fmt.Errorf("failed to deserialize greet reqeuest: %w", err)
	}
	request.Visits = visits

	// next, we will forward a message to a special greeter function,
	// that will compute a personalized greeting based on the number
	// of visits that this person has been seen.
	ctx.Send(statefun.MessageBuilder{
		Target: statefun.Address{
			FunctionType: GreeterTypeName,
			Id:           request.Name,
		},
		Value:     request,
		ValueType: GreetRequestType,
	})

	return nil
}

func Greeter(ctx statefun.Context, message statefun.Message) error {
	var request GreetRequest
	if err := message.As(GreetRequestType, &request); err != nil {
		return fmt.Errorf("failed to deserialize greet reqeuest: %w", err)
	}

	greeting := computeGreeting(request.Name, request.Visits)

	egressRecord := EgressRecord {
		Topic: "greetings",
		Payload: greeting,
	}

	ctx.SendEgress(statefun.GenericEgressBuilder{
		Target: PlaygroundEgressTypeName,
		Value:  egressRecord,
		ValueType: EgressRecordType,
	})

	return nil
}

func computeGreeting(name string, visits int32) string {
	templates := []string{"", "Welcome %s", "Nice to see you again %s", "Third time is the charm %s"}
	if visits < int32(len(templates)) {
		return fmt.Sprintf(templates[visits], name)
	}

	return fmt.Sprintf("Nice to see you for the %d-th time %s!", visits, name)
}

func main() {

	builder := statefun.StatefulFunctionsBuilder()

	person := &Person{
		Visits: statefun.ValueSpec{
			Name:      "visits",
			ValueType: statefun.Int32Type,
		},
	}
	_ = builder.WithSpec(statefun.StatefulFunctionSpec{
		FunctionType: PersonTypeName,
		States:       []statefun.ValueSpec{person.Visits},
		Function:     person,
	})

	_ = builder.WithSpec(statefun.StatefulFunctionSpec{
		FunctionType: GreeterTypeName,
		Function:     statefun.StatefulFunctionPointer(Greeter),
	})

	http.Handle("/statefun", builder.AsHandler())
	_ = http.ListenAndServe(":8000", nil)
}

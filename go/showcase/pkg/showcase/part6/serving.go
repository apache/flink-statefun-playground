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
	"github.com/apache/flink-statefun/statefun-sdk-go/v3/pkg/statefun"
	"log"
	"net/http"
)

// This is where everything comes together. In this section, we'd like to guide
// you through how to expose the functions you implemented via any HTTP webserver.
// In particular, stateful functions implements the standard GoLang http.Handler
// interface.
//
// This program is a standalone Go program that can be ran directly in the IDE,
// and exposes some functions using the standard Go web server. In practice you
// can choose any web server that can handle HTTP request and responses, even AWS Lambda.
//
// In addition to that, you will also run a "toy" set of StateFun runtime processes in
// a separate program to see StateFun in action for the first time. The StateFun processes
// are responsible  for routing and dispatching the messages from ingresses, functions,
// and egresses, and also manages the function state storages to be consistent and fault-tolerant
// This is all done transparently in the background!
//
// Next Steps
//
// The setup you executed in this tutorial is NOT how you would normally deploy
// StateFun processes and functions. It's a rather simplified setup to allow you to
// explore the interaction between functions and the StateFun processes by setting debugger
// breakpoints in the function code.
//
// We recommend now to take a look at a slightly more realistic setup, using docker-compose
// in the Greeter Example https://github.com/apache/flink-statefun-playground/java/greeter/
func main() {

	// register the functions that you want to serve...
	builder := statefun.StatefulFunctionsBuilder()
	if err := builder.WithSpec(UserFnSpec()); err != nil {
		log.Fatalf("failed to register user function: %s", err)
	}

	if err := builder.WithSpec(GreetingSpec()); err != nil {
		log.Fatalf("failed to register greeting function: %s", err)
	}

	// ... and build a request-reply handler for the registered functions, which understands how to
	// decode invocation requests dispatched from StateFun / encode side-effects (e.g. state storage
	// updates, or invoking other functions) as responses to be handled by StateFun.
	handler := builder.AsHandler()

	// Use the request-reply handler along with your favorite HTTP web server framework
	// to serve the functions!
	http.Handle("/", handler)
	_ = http.ListenAndServe(":8000", nil)
}

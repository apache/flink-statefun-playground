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

package part2

import (
	"github.com/apache/flink-statefun/statefun-sdk-go/v3/pkg/statefun"
	"statefun.io/showcase/pkg/showcase/part1"
	"time"
)

var TargetFnTypeName = statefun.TypeNameFrom("showcase.fns/some-other-fn")

// Messaging
//   Showcase Part 2: Messaging Primitives
//
//   ============================
//
//   This function demonstrates how to send messages to
//   other functions.
//
//   ============================
//
//   Besides sending messages to other functions, functions may also send
//   Messages to the outside world via egresses. This is showcased by the next
//   part of the series, EgressFn.
func Messaging(ctx statefun.Context, _ statefun.Message) error {
	// You send messages to functions simply by specifying the target function's typename
	// and the target instance id for that for function; StateFun handles the routing for you,
	// without the need of any means for service discovery.
	target := statefun.Address{
		FunctionType: TargetFnTypeName,
		Id:           "target-instance-id",
	}

	// you can directly send primitive type values as messages, ...
	ctx.Send(statefun.MessageBuilder{
		Target: target,
		Value:  true,
	})
	ctx.Send(statefun.MessageBuilder{
		Target: target,
		Value:  int32(123),
	})
	ctx.Send(statefun.MessageBuilder{
		Target: target,
		Value:  int64(-19911108123046639),
	})
	ctx.Send(statefun.MessageBuilder{
		Target: target,
		Value:  float32(3.14159),
	})
	ctx.Send(statefun.MessageBuilder{
		Target: target,
		Value:  float64(3.14159e+11),
	})
	ctx.Send(statefun.MessageBuilder{
		Target: target,
		Value:  "hello world",
	})

	// ... or, in general, a value of any custom defined type.
	ctx.Send(statefun.MessageBuilder{
		Target: target,
		Value: part1.UserLogin{
			UserId:    "id",
			UserName:  "john smith",
			LoginType: part1.MOBILE,
		},
		ValueType: part1.UserLoginType,
	})

	// You can send messsages to any function including yourself!
	ctx.Send(statefun.MessageBuilder{
		Target: ctx.Self(),
		Value:  "hello me!",
	})

	// Additionally, you may ask StateFun to send out a message after a specified delay.
	// A common usage pattern is to send delayed messages to yourself to model timer triggers.
	ctx.SendAfter(time.Duration(10)*time.Minute, statefun.MessageBuilder{
		Target: target,
		Value: part1.UserLogin{
			UserId:    "id",
			UserName:  "john smith",
			LoginType: part1.MOBILE,
		},
		ValueType: part1.UserLoginType,
	})

	// None of the above sends are blocking operations.
	// All side-effects, such as messaging other functions, are collected
	// and happen after this method returns.
	return nil
}

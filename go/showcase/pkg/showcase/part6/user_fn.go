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
	"statefun.io/showcase/pkg/showcase/part1"
	"time"
)

// UserFnTypeName Every registered function needs to be associated with a unique TypeName.
// A function's typename is usd by other functions and ingresses to have messages
// addressed to them.
var UserFnTypeName = statefun.TypeNameFrom("showcase.fns/user")

type UserFn struct {
	Seen                statefun.ValueSpec
	SeenTimestampMillis statefun.ValueSpec
}

// UserFnSpec specification of the functions. This can be used
// to register the function - see main.
func UserFnSpec() statefun.StatefulFunctionSpec {
	function := UserFn{
		Seen: statefun.ValueSpec{
			Name:      "seen_count",
			ValueType: statefun.Int32Type,
		},
		SeenTimestampMillis: statefun.ValueSpec{
			Name:      "seen_timestamp_millis",
			ValueType: statefun.Int64Type,
		},
	}

	return statefun.StatefulFunctionSpec{
		FunctionType: UserFnTypeName,
		States:       []statefun.ValueSpec{function.Seen, function.SeenTimestampMillis},
		Function:     function,
	}
}

func (u UserFn) Invoke(ctx statefun.Context, message statefun.Message) error {
	if !message.Is(part1.UserLoginType) {
		return fmt.Errorf("unexpected message type %s", message.ValueTypeName())
	}

	var login part1.UserLogin
	_ = message.As(part1.UserLoginType, &login)

	var count int32
	_ = ctx.Storage().Get(u.Seen, &count)
	count++

	now := time.Now().UnixNano() / int64(time.Millisecond)
	var lastSeenTimestampMillis int64
	if exists := ctx.Storage().Get(u.SeenTimestampMillis, &lastSeenTimestampMillis); !exists {
		lastSeenTimestampMillis = now
	}

	ctx.Storage().Set(u.Seen, count)
	ctx.Storage().Set(u.SeenTimestampMillis, lastSeenTimestampMillis)

	profile := UserProfile{
		Name:            login.UserName,
		LoginLocation:   login.LoginType.String(),
		SeenCount:       count,
		LastSeenDeltaMs: now - lastSeenTimestampMillis,
	}

	ctx.Send(statefun.MessageBuilder{
		Target: statefun.Address{
			FunctionType: GreetingTypeName,
			Id:           login.UserName,
		},
		Value:     profile,
		ValueType: UserProfileType,
	})

	return nil
}

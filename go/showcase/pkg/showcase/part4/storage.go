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

package part4

import (
	"github.com/apache/flink-statefun/statefun-sdk-go/v3/pkg/statefun"
	"statefun.io/showcase/pkg/showcase/part1"
	"time"
)

// StateStorageFn
//  Showcase Part 3: Function State Storage
//
//  ============================
//
//  One of StateFun's most powerful features is its capability of managing function state in a
//  consistent and fault-tolerant manner. This function demonstrates accessing and manipulating
//  persisted function state that is managed by StateFun.
//
//  ============================
//
//  In some applications, functions may need to retrieve existing data from external services. Take a
//  look at the next series in this showcase, AsyncOpsFn, on how to perform asynchronous operations in your functions.
type StateStorageFn struct {
	IntValue       statefun.ValueSpec
	BoolValue      statefun.ValueSpec
	UserLoginValue statefun.ValueSpec
}

func NewStateStorageFn() StateStorageFn {
	return StateStorageFn{
		IntValue: statefun.ValueSpec{
			Name:      "int_value",
			ValueType: statefun.Int32Type,
		},
		BoolValue: statefun.ValueSpec{
			Name:       "bool_value",
			ValueType:  statefun.BoolType,
			Expiration: statefun.ExpireAfterCall(time.Duration(5) * time.Hour),
		},
		UserLoginValue: statefun.ValueSpec{
			Name:      "user_login_value",
			ValueType: part1.UserLoginType,
		},
	}
}

func (s StateStorageFn) Invoke(ctx statefun.Context, msg statefun.Message) error {
	// each function invocation gets access to storage that is scoped to the current function
	// instance's address, i.e. (function typename, instance id). For example, if (UserFn, "Gordon")
	// was invoked, the values you get access to belongs specifically to user Gordon.
	storage := ctx.Storage()

	// you can think of the storage having several "columns", with each column identified by an
	// uniquely named ValueSpec. Using the ValueSpec instances, you can access the value of
	// individual columns. Get returns a boolean indicating whether there is an existing
	// value, so you can differentiate between missing and the zero value.
	var storedInt int32
	_ = storage.Get(s.IntValue, &storedInt)

	var boolValue bool
	exists := storage.Get(s.BoolValue, &boolValue)

	var login part1.UserLogin
	_ = storage.Get(s.UserLoginValue, &login)

	// the ValueSpec instance is also used for manipulating the stored values, e.g. updating ...
	storage.Set(s.IntValue, storedInt+1)

	if !exists {
		boolValue = true
	}

	storage.Set(s.BoolValue, boolValue)

	// ... or clearing the value!
	storage.Remove(s.UserLoginValue)

	return nil
}

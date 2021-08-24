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

package part1

import (
	"fmt"
	"github.com/apache/flink-statefun/statefun-sdk-go/v3/pkg/statefun"
)

// Types
//  Showcase Part 1: Type System
//  ============================
//  This function demonstrates StateFun's type system using the GoLang SDK.
//
//  Core Type abstraction
//  =====================
//  The core abstraction used by StateFun's type system is the Type interface, which
//  consists of a few things that StateFun uses to handle messages and state values:
//
//  A TypeName to identify the type.
//  A TypeSerializer for serializing and deserializing instances of the type.
//
//  Cross-language primitive types
//  ==============================
//  StateFun's type system has cross-language support for common primitive types, such as boolean,
//  integer, long, etc. These primitive types have built-in Types implemented for them
//  already, with predefined typenames.
//
//  This is of course all transparent for the user, so you don't need to worry about it. Functions
//  implemented in various languages (e.g. Java or Python) can message each other by directly sending
//  supported primitive values as message arguments. Moreover, the type system is used for state
//  values as well; so, you can expect that a function can safely read previous state after
//  reimplementing it in a different language. We'll cover more on state storage access in later
//  parts of the showcase series.
//
//  Common custom types (e.g. JSON or Protobuf)
//  ===========================================
//  The type system is also very easily extensible to support custom message types, such as JSON
//  or Protobuf messages. This is just a matter of implementing your own Type with a custom
//  typename and serializer.
//
//  StateFun makes this super easy by providing builder utilities to help you create a simple
//  Type. Take a look at showcase_custom_types.py for few recommended ways to quickly create a StateFun Type
//  for your JSON or Protobuf messages.
func Types(_ statefun.Context, message statefun.Message) error {
	// All values, including messages and storage values, are handled via StateFun's type system.
	// StateFun ships built-in primitive types that handles de-/serialization of messages across
	// functions:
	if message.IsBool() {
		fmt.Printf("I've got a message with a boolean %v", message.AsBool())
	} else if message.IsInt32() {
		fmt.Printf("I've got a message with an int32 %v", message.AsInt32())
	} else if message.IsInt64() {
		fmt.Printf("I've got a message with an int64 %v", message.AsInt64())
	} else if message.IsFloat32() {
		fmt.Printf("I've got a message with a float32 %v", message.AsFloat32())
	} else if message.IsFloat64() {
		fmt.Printf("I've got a message with a float64 %v", message.AsFloat64())
	} else if message.IsString() {
		fmt.Printf("I've got a message with a string %v", message.AsString())
	} else if message.Is(UserLoginType) {
		var login UserLogin
		if err := message.As(UserLoginType, &login); err != nil {
			return fmt.Errorf("failed to deserialize user login: %w", err)
		}
		fmt.Printf("I've got a message with a login event %s", login)
	}

	return nil
}

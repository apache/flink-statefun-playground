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

package part5

import (
	"github.com/apache/flink-statefun/statefun-sdk-go/v3/pkg/statefun"
	"time"
)

// AsyncOpsFn
//   Showcase Part 5: Asyncronous operations
//
//   ============================
//
//   This function demonstrates performing async operations
//   during a function invocation. It is a common scenario for
//   functions to have external dependencies in order for it to
//   complete its work, such as fetching enrichment information from
//   a remote database. statefun.Context is a valid context.Context and
//   can be used to coordinate work across multiple channels.
//
//   ============================
//
//   After learning everything about implementing a stateful function, the last bit of this showcase
//   series demonstrates how to expose the functions you implemented so that the StateFun runtime can
//   reach them and dispatch message invocations. Take a look now at the showcase.go.
type AsyncOpsFn struct {
	UserAge     statefun.ValueSpec
	FriendsList statefun.ValueSpec
}

func NewAsyncOpsFn() AsyncOpsFn {
	return AsyncOpsFn{
		UserAge: statefun.ValueSpec{
			Name:      "age",
			ValueType: statefun.Int32Type,
		},
		FriendsList: statefun.ValueSpec{
			Name:      "friends",
			ValueType: statefun.MakeJsonType(statefun.TypeNameFrom("statefun.types/friends")),
		},
	}
}

func (a AsyncOpsFn) Invoke(ctx statefun.Context, message statefun.Message) error {
	username := ctx.Self().Id

	ageChannel := getAgeFromRemoteDb(ctx, username)
	friendsChannel := getFriendsListFromAnotherRemoteDatabase(ctx, username)

	var age int32
	var friends []string

	finished := false

	for !finished {
		select {
		case <-ctx.Done():
			return ctx.Err()
		case age = <-ageChannel:
			if friends != nil {
				finished = true
			}
		case friends = <-friendsChannel:
			if age != 0 {
				finished = true
			}
		}
	}

	ctx.Storage().Set(a.UserAge, age)
	ctx.Storage().Set(a.FriendsList, friends)

	return nil
}

// getAgeFromRemoteDb is a mock async request to fetch a users
// age information from a remote database.
func getAgeFromRemoteDb(_ statefun.Context, _ string) <-chan int32 {
	c := make(chan int32, 1)

	go func() {
		time.Sleep(2 * time.Second)
		c <- 29
		close(c)
	}()

	return c
}

// getFriendsListFromAnotherRemoteDatabase is  mock asynchronouse request to fetch a
// user's friends list from a remote database
func getFriendsListFromAnotherRemoteDatabase(_ statefun.Context, _ string) <-chan []string {
	c := make(chan []string, 1)

	go func() {
		time.Sleep(3 * time.Second)
		c <- []string{"Igal", "Marta", "Stephan", "Seth", "Konstantin"}
		close(c)
	}()

	return c
}

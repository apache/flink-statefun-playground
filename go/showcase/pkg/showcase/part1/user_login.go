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
	"bytes"
	"encoding/json"
	"fmt"
	"github.com/apache/flink-statefun/statefun-sdk-go/v3/pkg/statefun"
	"strings"
)

// UserLoginType is a custom statefun type that marshals data using
// standard Go JSON marshaling.
var UserLoginType = statefun.MakeJsonType(statefun.TypeNameFrom("showcase.types/userlogin"))

// UserLogin is a struct for user login events, which are passed around
// in JSON form.
type UserLogin struct {
	UserId    string    `json:"user_id"`
	UserName  string    `json:"user_name"`
	LoginType LoginType `json:"login_type"`
}

func (u UserLogin) String() string {
	b, _ := json.Marshal(&u)
	return string(b)
}

type LoginType int

const (
	WEB LoginType = iota
	MOBILE
)

var toString = map[LoginType]string{
	WEB:    "WEB",
	MOBILE: "MOBILE",
}

var toId = map[string]LoginType{
	"WEB":    WEB,
	"MOBILE": MOBILE,
}

func (l LoginType) String() string {
	return toString[l]
}

func (l LoginType) MarshalJSON() ([]byte, error) {
	buffer := bytes.NewBufferString(`"`)
	buffer.WriteString(l.String())
	buffer.WriteString(`"`)
	return buffer.Bytes(), nil
}

func (l *LoginType) UnmarshalJSON(b []byte) error {
	var s string
	if err := json.Unmarshal(b, &s); err != nil {
		return err
	}

	s = strings.ToUpper(s)

	if login, ok := toId[s]; !ok {
		return fmt.Errorf("unknown login type %s", s)
	} else {
		*l = login
	}

	return nil
}

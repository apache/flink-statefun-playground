/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.flink.statefun.playground.java.showcase.part1.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.flink.statefun.sdk.java.types.Type;

/**
 * Java Bean class for user login events, which are passed around in JSON form.
 *
 * <p>Please take a look at {@link MyCustomTypes#USER_LOGIN_JSON_TYPE} on how a custom StateFun
 * {@link Type} can be created to let the Java SDK understand how to marshal and unmarshal JSON
 * strings (either used as invocation messages or state values) to this Java Bean class.
 */
public final class UserLogin {

  public enum LoginType {
    WEB,
    MOBILE
  }

  @JsonProperty("user_id")
  private String userId;

  @JsonProperty("user_name")
  private String userName;

  @JsonProperty("login_type")
  private LoginType loginType;

  public UserLogin() {}

  public String getUserId() {
    return userId;
  }

  public String getUserName() {
    return userName;
  }

  public LoginType getLoginType() {
    return loginType;
  }
}

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
package org.apache.flink.statefun.playground.aws;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2ProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2ProxyResponseEvent;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.apache.flink.statefun.sdk.java.handler.RequestReplyHandler;
import org.apache.flink.statefun.sdk.java.slice.Slice;
import org.apache.flink.statefun.sdk.java.slice.Slices;

/** Delegates AWS API Gateway requests to a StateFun {@link RequestReplyHandler}. */
final class APIGatewayStateFunHandlerProxy {

  /** Standard required response headers for StateFun */
  private static final Map<String, String> RESPONSE_HEADER = new HashMap<>(1);

  static {
    RESPONSE_HEADER.put("Content-Type", "application/octet-stream");
  }

  private final RequestReplyHandler stateFunHandler;

  APIGatewayStateFunHandlerProxy(RequestReplyHandler stateFunHandler) {
    this.stateFunHandler = Objects.requireNonNull(stateFunHandler);
  }

  public APIGatewayV2ProxyResponseEvent handle(APIGatewayV2ProxyRequestEvent request) {
    // Binary blobs (the invocation request) are always base64 encoded by the API Gateway
    final byte[] decoded = Base64.getDecoder().decode(request.getBody());

    final Slice responseSlice = stateFunHandler.handle(Slices.wrap(decoded)).join();
    return createResponse(responseSlice);
  }

  private static APIGatewayV2ProxyResponseEvent createResponse(Slice responseSlice) {
    final APIGatewayV2ProxyResponseEvent response = new APIGatewayV2ProxyResponseEvent();
    response.setHeaders(RESPONSE_HEADER);
    response.setIsBase64Encoded(true);
    response.setStatusCode(200);
    response.setBody(Base64.getEncoder().encodeToString(responseSlice.toByteArray()));

    return response;
  }
}

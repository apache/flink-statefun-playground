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

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2ProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2ProxyResponseEvent;
import org.apache.flink.statefun.sdk.java.StatefulFunctions;
import org.apache.flink.statefun.sdk.java.handler.RequestReplyHandler;

/**
 * An <a href="https://aws.amazon.com/lambda/">AWS Lambda</a> {@link RequestHandler} implementation
 * for serving StateFun functions through <a href="https://aws.amazon.com/api-gateway/">AWS API
 * Gateway</a>.
 *
 * <p>It is straightforward to integrate a StateFun {@link RequestReplyHandler} within an AWS
 * Lambda's {@link RequestHandler}. As shown below, all AWS API Gateway HTTP requests received by
 * the AWS Lambda function is simply delegated to a StateFun {@link RequestReplyHandler} via a
 * {@link APIGatewayStateFunHandlerProxy}. Take a closer look at the {@link
 * APIGatewayStateFunHandlerProxy} on details of how the request and response was handled.
 */
public final class GreeterAwsLambdaHandler
    implements RequestHandler<APIGatewayV2ProxyRequestEvent, APIGatewayV2ProxyResponseEvent> {

  private static final APIGatewayStateFunHandlerProxy HANDLER;

  static {
    final StatefulFunctions functions = new StatefulFunctions();
    functions.withStatefulFunction(GreeterFn.SPEC);

    HANDLER = new APIGatewayStateFunHandlerProxy(functions.requestReplyHandler());
  }

  @Override
  public APIGatewayV2ProxyResponseEvent handleRequest(
      APIGatewayV2ProxyRequestEvent event, Context context) {
    return HANDLER.handle(event);
  }
}

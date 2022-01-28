# Serving StateFun Functions with AWS Lambda

This tutorial is intended to demonstrate how you can serve your functions with [AWS Lambda](https://aws.amazon.com/lambda/),
a popular FaaS platform for running code with serverless deployments. With the rapid elasticity and simplicity of FaaS
platforms, this makes it very easy to scale out your StateFun functions.

If you are still new to StateFun and unfamiliar with the overall architecture of a StateFun application, we highly
recommend to first take a look at some of the demo tutorials under the language-specific directories (such as the
Greeter examples for [Java](../../java/greeter) or [Python](../../python/greeter)).

## Deployment Steps

### Build an AWS Lambda code package

We're demonstrating Java here, so the code package you submit to AWS Lambda will be a JAR.

```
$ mvn clean install
# or, if you don't have Maven installed and would like to use Docker:
$ docker run -it --rm --name build -v "$(pwd)":/usr/src/mymaven -w /usr/src/mymaven maven:3.6.3-jdk-8 mvn clean install
```

This builds an uber JAR containing all required dependencies by the AWS Lambda runtime at `target/aws-lambda-example*.jar`.

### Deploy to AWS Lambda

- Upload this JAR to AWS Lambda. You can do this [manually via the web console](https://docs.aws.amazon.com/lambda/latest/dg/getting-started-create-function.html),
or using the [AWS CLI](https://aws.amazon.com/cli/).

- The handler method should be set to `org.apache.flink.statefun.playground.aws.GreeterAwsLambdaHandler::handleRequest`.
That refers to the entrypoint handle method you'll find in the [code](src/main/java/org/apache/flink/statefun/playground/aws/GreeterAwsLambdaHandler.java).

- After you upload and create the new AWS Lambda function, configure it to be
[triggered by an API Gateway HTTP endpoint](https://docs.aws.amazon.com/lambda/latest/dg/services-apigateway.html).

After completing this step, you should now have a HTTP endpoint URL (e.g. https://abcdxyz.execute-api.us-west-1.amazonaws.com/default/my-aws-lambda-fn)
for invoking your StateFun functions.

### Configure StateFun Module Specification

The AWS Lambda endpoint URL should be configured as a function endpoint in your [Module Specification](https://ci.apache.org/projects/flink/flink-statefun-docs-release-3.2/docs/deployment/module/).

Note how you may use [templating](https://ci.apache.org/projects/flink/flink-statefun-docs-release-3.2/docs/deployment/module/#url-template)
in the endpoint URL for multiple functions under the same namespace This allows you to add new AWS Lambda functions,
potentially individually serving different StateFun functions, under separate paths of the same AWS API Gateway endpoint.
This provides flexibility to upgrade your StateFun application by dynamically adding new functions without having to restart the StateFun runtime.

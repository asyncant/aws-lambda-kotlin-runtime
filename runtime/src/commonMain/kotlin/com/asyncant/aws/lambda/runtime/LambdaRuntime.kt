package com.asyncant.aws.lambda.runtime

import com.asyncant.platform.requireEnv

/** Convenience wrapper around [runBinaryLambda] that accepts [String]s and ignores the event context. */
inline fun runLambda(crossinline handler: (String) -> String) = runLambda { event, _ -> handler.invoke(event) }

/** Convenience wrapper around [runBinaryLambda] that accepts [String]s. */
inline fun runLambda(crossinline handler: (event: String, context: EventContext) -> String) {
  runBinaryLambda { event, context -> handler.invoke(event.decodeToString(), context).encodeToByteArray() }
}

/**
 * Waits for lambda invocation events to arrive and calls the given handler for each event and returns the handlers
 * result as the result of the invocation.
 *
 * @param handler Lambda invocation handler that accepts two parameters, first being a ByteArray-based event and second
 *   an event context. The result returned by the handler is returned as the result of the Lambda invocation.
 *   When the handler throws an exception, the invocation will be reported as failed with the exception's message.
 */
inline fun runBinaryLambda(crossinline handler: (event: ByteArray, context: EventContext) -> ByteArray) {
  val httpClient = AwsLambdaRuntimeHttp11Client(requireEnv("AWS_LAMBDA_RUNTIME_API"))
  val client = LambdaClient(httpClient)
  while (true) {
    val event = client.retrieveNextEvent()
    try {
      val result = handler(event.data, event.context)
      client.sendResponse(event.context, result)
    } catch (e: Exception) {
      e.printStackTrace()
      client.sendError(event.context, "UnhandledException", e.message ?: e.stackTraceToString())
      break
    }
  }
  httpClient.close()
}

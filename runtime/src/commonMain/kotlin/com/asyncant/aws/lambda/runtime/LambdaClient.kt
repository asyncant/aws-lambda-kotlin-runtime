package com.asyncant.aws.lambda.runtime

import com.asyncant.http.BlockingHttpClient
import com.asyncant.http.HttpResponse
import com.asyncant.json.toJsonString
import com.asyncant.platform.requireEnv

/** Communicates with the Lambda environment about events to process. */
class LambdaClient(private val httpClient: BlockingHttpClient) {
  private val invokeUrl = "http://${requireEnv("AWS_LAMBDA_RUNTIME_API")}/2018-06-01/runtime/invocation"

  fun retrieveNextEvent(): InvocationEvent {
    val response = httpClient.get("${invokeUrl}/next", emptyMap())
    require(response.statusCode == 200) { "Status 200 expected for next invocation, got: $response" }
    return InvocationEvent(response.body, contextFromResponse(response))
  }

  fun sendResponse(event: EventContext, result: ByteArray): HttpResponse {
    return httpClient.post("${invokeUrl}/${event.requestId}/response", result, emptyMap())
  }

  fun sendError(event: EventContext, errorType: String, errorMessage: String): HttpResponse {
    val body = """{"errorMessage":${toJsonString(errorMessage)},"errorType":${toJsonString(errorType)}}"""
    val headers = mapOf("Lambda-Runtime-Function-Error-Type" to "Unhandled")
    return httpClient.post("${invokeUrl}/${event.requestId}/error", body.encodeToByteArray(), headers)
  }

  private fun contextFromResponse(response: HttpResponse): EventContext {
    val requestId = requireNotNull(response.headers["Lambda-Runtime-Aws-Request-Id"]).first()
    val deadlineMs = requireNotNull(response.headers["Lambda-Runtime-Deadline-Ms"]).first().toLong()
    val invokedFunctionArn = requireNotNull(response.headers["Lambda-Runtime-Invoked-Function-Arn"]).first()
    val clientContext = response.headers["Lambda-Runtime-Client-Context"]?.first()
    val cognitoIdentity = response.headers["Lambda-Runtime-Cognito-Identity"]?.first()
    return EventContext(requestId, deadlineMs, invokedFunctionArn, clientContext, cognitoIdentity)
  }
}

class InvocationEvent(val data: ByteArray, val context: EventContext)
data class EventContext(
  val requestId: String,
  val deadlineMs: Long,
  val invokedFunctionArn: String,
  val clientContext: String?,
  val cognitoIdentity: String?
)


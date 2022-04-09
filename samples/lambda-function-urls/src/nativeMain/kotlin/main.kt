import com.asyncant.aws.lambda.runtime.runLambda
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.experimental.or

val json = Json { ignoreUnknownKeys = true }

fun main() = runLambda { event ->
  val request: LambdaFunctionUrlRequest = json.decodeFromString(event)

  val requestBody = if (request.isBase64Encoded) request.body?.decodeFromBase64()?.decodeToString() else request.body

  val responseBody = """
    ################################################################
    Hello from kotlin/native lambda function urls sample!
    ################################################################
    This sample was invoked with:
    - Method: ${request.requestContext.http.method}
    - Path: ${request.rawPath}
    - Parameters: ${request.queryStringParameters}
    - Headers: ${request.headers}
    - Body: $requestBody
  """.trimIndent()
  val response = LambdaFunctionUrlResponse(200, responseBody, false)

  Json.encodeToString(response)
}

@Serializable
data class LambdaFunctionUrlResponse(val statusCode: Int, val body: String, val isBase64Encoded: Boolean)

@Serializable
data class LambdaFunctionUrlRequest(
  val rawPath: String,
  val headers: Map<String, String>,
  val queryStringParameters: Map<String, String> = emptyMap(),
  val requestContext: LambdaFunctionUrlRequestContext,
  val body: String? = null,
  val isBase64Encoded: Boolean,
)

@Serializable
data class LambdaFunctionUrlRequestContext(val http: LambdaFunctionUrlHttp)

@Serializable
data class LambdaFunctionUrlHttp(val method: String)

private fun String.decodeFromBase64(): ByteArray {
  val padding = if (this[length - 2] == '=') 2 else if (this[length - 1] == '=') 1 else 0
  val lastByteIndex = (3 * (length / 4) - padding) - 1
  val result = ByteArray(lastByteIndex + 1)
  var bitIndex = 0
  for (c in this.substringBefore('=')) {
    val value = BASE64_TABLE.indexOf(c) shl 2
    val bitsFilled = bitIndex % 8
    val byteIndex = bitIndex / 8
    result[byteIndex] = result[byteIndex] or (value ushr bitsFilled).toByte()
    if (bitsFilled > 2 && byteIndex < lastByteIndex) {
      result[byteIndex + 1] = result[byteIndex + 1] or (value shl (8 - bitsFilled)).toByte()
    }
    bitIndex += 6
  }
  return result
}

private const val BASE64_TABLE = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"

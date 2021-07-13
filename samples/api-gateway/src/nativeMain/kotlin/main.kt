import com.asyncant.aws.lambda.runtime.runLambda

fun main() = runLambda { event ->
  println("Received event: $event")
  """
    {
      "statusCode": 200,
      "body":"Hello world!"
    }
  """.trimIndent()
}
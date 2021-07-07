import com.asyncant.aws.lambda.runtime.runLambda

fun main() = runLambda { event ->
  println("Received event: $event")
  "Hello world!"
}
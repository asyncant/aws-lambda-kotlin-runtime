# AWS Lambda Kotlin Runtime

A low overhead kotlin/native based custom runtime for [AWS Lambda](https://aws.amazon.com/lambda/):
```
// Cold start
REPORT Duration: 0.87 ms  Billed Duration: 30 ms  Memory Size: 128 MB  Max Memory Used: 36 MB  Init Duration: 28.78 ms
// Warm invocation
REPORT Duration: 0.64 ms  Billed Duration: 1 ms  Memory Size: 128 MB  Max Memory Used: 36 MB
```

## Usage

```kotlin
fun main() = runLambda { event ->
  println("Received: $event")
  "Hello world!"
}
```

Or with initialization code:
```kotlin
fun main() {
  /* << Initialization code >> */

  runLambda { event, context ->
    /* << Handle event >> */
  }
}
```

## Installation

This project is still a work in progress and as such not yet published to an artifact repository, in the meantime it
can be included as a dependency through Gradle. The configuration below will instruct Gradle to pull the sources from
GitHub, compile them locally and include them as a dependency for a project.

`settings.gradle.kts`:
```kotlin
sourceControl {
  gitRepository(java.net.URI("https://github.com/asyncant/aws-lambda-kotlin-runtime.git")) {
    producesModule("com.asyncant.aws.lambda")
  }
}
```
`build.gradle.kts`:
```kotlin
  implementation("com.asyncant.aws.lambda:runtime:0.1-SNAPSHOT")
```



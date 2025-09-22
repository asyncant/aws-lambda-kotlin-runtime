# AWS Lambda Kotlin Runtime

A low overhead kotlin/native based custom runtime for [AWS Lambda](https://aws.amazon.com/lambda/) with a cold start
between 9 and 15ms:
```
REPORT Duration: 0.58 ms  Billed Duration: 12 ms  Memory Size: 128 MB  Max Memory Used: 13 MB  Init Duration: 10.48 ms
```
And a warm invocation <1ms:
```
REPORT Duration: 0.53 ms  Billed Duration: 1 ms  Memory Size: 128 MB  Max Memory Used: 16 MB
```

The runtime supports both x86_64 and ARM64 architectures.

## Usage

```kotlin
import com.asyncant.aws.lambda.runtime.runLambda

fun main() = runLambda { event ->
  println("Received: $event")
  "Hello world!"
}
```

Or with initialization code and context:
```kotlin
import com.asyncant.aws.lambda.runtime.runLambda

fun main() {
  /* << Initialization code >> */

  runLambda { event, context ->
    /* << Handle event >> */
  }
}
```

## Installation

The library can be found on Maven Central [here](https://search.maven.org/artifact/com.asyncant.aws.lambda/runtime).

### Gradle

```kotlin
implementation("com.asyncant.aws.lambda:runtime:0.9.5")
```

### Maven

```xml
<dependency>
    <groupId>com.asyncant.aws.lambda</groupId>
    <artifactId>runtime</artifactId>
    <version>0.9.5</version>
</dependency>
```

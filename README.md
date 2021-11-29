# AWS Lambda Kotlin Runtime

A low overhead kotlin/native based custom runtime for [AWS Lambda](https://aws.amazon.com/lambda/) with a cold start
between 9 and 15ms:
```
REPORT Duration: 0.60 ms  Billed Duration: 12 ms  Memory Size: 128 MB  Max Memory Used: 25 MB  Init Duration: 10.49 ms
```
And a warm invocation <1ms:
```
REPORT Duration: 0.53 ms  Billed Duration: 1 ms  Memory Size: 128 MB  Max Memory Used: 25 MB
```

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
implementation("com.asyncant.aws.lambda:runtime:0.8")
```

### Maven

```xml
<dependency>
    <groupId>com.asyncant.aws.lambda</groupId>
    <artifactId>runtime</artifactId>
    <version>0.8</version>
</dependency>
```

### Non linux_x64 targets

The artifacts on Maven Central target `linux_64` matching the AWS Lambda environment, to execute the library in other
environments (e.g. while developing), the project can also be included as a source dependency through Gradle.
The configuration below will instruct Gradle to pull the sources from GitHub, compile them locally and include them as
a dependency for a project.

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
  implementation("com.asyncant.aws.lambda:runtime:0.8")
```

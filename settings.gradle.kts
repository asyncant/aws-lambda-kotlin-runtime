include(
  "runtime",
  "samples:hello-world",
  "samples:hello-world-graalvm",
  "samples:hello-world-arm64",
  "samples:api-gateway",
  "samples:lambda-function-urls"
)

dependencyResolutionManagement {
  @Suppress("UnstableApiUsage")
  repositories {
    mavenCentral()
  }

  versionCatalogs {
    create("libs") {
      plugin("kotlinserialization.plugin", "org.jetbrains.kotlin.plugin.serialization").version("2.3.21")
      library("kotlinserialization", "org.jetbrains.kotlinx:kotlinx-serialization-json:1.11.0")
    }
  }
}

include(
  "runtime",
  "samples:hello-world",
  "samples:hello-world-al2023",
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
      library("kotlinserialization", "org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.1")
    }
  }
}

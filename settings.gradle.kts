include("runtime", "samples:hello-world", "samples:api-gateway", "samples:lambda-function-urls")

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

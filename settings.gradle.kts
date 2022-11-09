include("runtime", "samples:hello-world", "samples:api-gateway", "samples:lambda-function-urls")

enableFeaturePreview("VERSION_CATALOGS")
dependencyResolutionManagement {
  repositories {
    mavenCentral()
  }

  versionCatalogs {
    create("libs") {
      library("kotlinserialization", "org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.1")
    }
  }
}

include("runtime", "samples:hello-world", "samples:api-gateway", "samples:lambda-function-urls")

enableFeaturePreview("VERSION_CATALOGS")
dependencyResolutionManagement {
  repositories {
    mavenCentral()
  }

  versionCatalogs {
    create("libs") {
      alias("kotlinserialization").to("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.1")
    }
  }
}

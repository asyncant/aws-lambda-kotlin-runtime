include("runtime", "samples:hello-world")

sourceControl {
  gitRepository(java.net.URI("https://github.com/asyncant/sha256-kt.git")) {
    producesModule("com.asyncant.crypto:sha256-kt")
  }
}

enableFeaturePreview("VERSION_CATALOGS")
dependencyResolutionManagement {
  versionCatalogs {
    create("libs") {
      alias("sha256").to("com.asyncant.crypto:sha256-kt:1.0")
    }
  }
}

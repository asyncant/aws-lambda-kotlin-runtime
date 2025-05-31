plugins {
  kotlin("multiplatform")
}
group = "com.asyncant.samples"
version = "0.0.123"

kotlin {
  linuxX64().apply {
    binaries {
      executable {
        entryPoint = "main"
      }
    }
  }

  sourceSets {
    nativeMain {
      dependencies {
        implementation(project(":runtime"))
      }
    }
  }
}

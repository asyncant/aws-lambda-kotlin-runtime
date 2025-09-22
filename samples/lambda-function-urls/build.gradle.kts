plugins {
  kotlin("multiplatform")
  kotlin("plugin.serialization") version "2.2.20"
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
        implementation(libs.kotlinserialization)
      }
    }
  }
}

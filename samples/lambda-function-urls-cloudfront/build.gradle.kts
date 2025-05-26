plugins {
  kotlin("multiplatform")
  kotlin("plugin.serialization") version "2.1.21"
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

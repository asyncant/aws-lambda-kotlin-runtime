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
        // Remove Kotlin's unused dependency on libcrypt as it is not present on Amazon Linux 2023.
        // * https://youtrack.jetbrains.com/issue/KT-55643
        linkerOpts("-Wl,--as-needed")
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

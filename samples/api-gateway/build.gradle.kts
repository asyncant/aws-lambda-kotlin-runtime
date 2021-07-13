plugins {
  kotlin("multiplatform")
  kotlin("plugin.serialization")
}
group = "com.asyncant.samples"
version = "0.0.123"

repositories {
  mavenCentral()
}
kotlin {
  val hostOs = System.getProperty("os.name")
  val isMingwX64 = hostOs.startsWith("Windows")
  val nativeTarget = when {
    hostOs == "Mac OS X" -> macosX64("native")
    hostOs == "Linux" -> linuxX64("native")
    isMingwX64 -> mingwX64("native")
    else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
  }

  nativeTarget.apply {
    binaries {
      executable {
        entryPoint = "main"
        linkerOpts("/usr/lib64/libcurl.so")
      }
    }
  }

  sourceSets {
    val nativeMain by getting {
      dependencies {
        implementation(project(":runtime"))
      }
    }
    val nativeTest by getting
  }
}

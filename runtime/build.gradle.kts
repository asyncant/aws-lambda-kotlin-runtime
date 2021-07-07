plugins {
  kotlin("multiplatform")
  kotlin("plugin.serialization")
  jacoco
}
group = rootProject.group
version = rootProject.version

repositories {
  mavenLocal()
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
    compilations["main"].cinterops {
      val libcurl by creating {
        includeDirs.headerFilterOnly("/usr/include")
      }
    }
  }
  sourceSets {
    val commonMain by getting {
      dependencies {
        implementation(kotlin("stdlib"))
      }
    }
    val commonTest by getting {
      dependencies {
        implementation(kotlin("test-common"))
        implementation(kotlin("test-annotations-common"))
      }
    }
    val nativeMain by getting

    val nativeTest by getting
  }
}

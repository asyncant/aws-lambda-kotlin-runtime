plugins {
  kotlin("multiplatform")
  id("org.jetbrains.dokka") version "1.4.32"
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

val javadocJar by tasks.registering(Jar::class) {
  dependsOn(tasks.dokkaHtml)
  archiveClassifier.set("javadoc")
  from(tasks.dokkaHtml.get().outputDirectory)
}

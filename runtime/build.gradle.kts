plugins {
  kotlin("multiplatform")
  id("org.jetbrains.dokka") version "1.6.10"
  jacoco
  id("maven-publish")
  id("signing")
}
group = rootProject.group
version = rootProject.version

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

publishing {
  repositories {
    maven {
      name = "sonatype"
      val releaseUrl = "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"
      val snapshotUrl = "https://s01.oss.sonatype.org/content/repositories/snapshots/"
      setUrl(if (version.toString().endsWith("SNAPSHOT")) snapshotUrl else releaseUrl)

      credentials(PasswordCredentials::class)
    }
  }

  publications.withType<MavenPublication> {
    artifact(javadocJar)
    pom {
      name.set("aws-lambda-kotlin-runtime")
      description.set("Kotlin/Native runtime for AWS Lambda.")
      url.set("https://github.com/asyncant/aws-lambda-kotlin-runtime")

      licenses {
        license {
          name.set("MIT")
          distribution.set("repo")
        }
      }

      developers {
        developer {
          id.set("asyncant")
          name.set("asyncant")
          url.set("http://www.asyncant.com")
        }
      }

      scm {
        connection.set("scm:git:git://github.com/asyncant/aws-lambda-kotlin-runtime.git")
        developerConnection.set("scm:git:ssh://github.com/asyncant/aws-lambda-kotlin-runtime.git")
        url.set("https://github.com/asyncant/aws-lambda-kotlin-runtime")
      }
    }
  }
}

signing {
  useGpgCmd()
  sign(publishing.publications)
}

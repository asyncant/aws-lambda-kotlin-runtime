plugins {
  kotlin("multiplatform")
  id("org.jetbrains.dokka") version "1.6.10"
  jacoco
  id("com.vanniktech.maven.publish") version "0.36.0"
  id("signing")
}
group = rootProject.group
version = rootProject.version

kotlin {
  jvm()
  linuxX64()
  linuxArm64()

  sourceSets {
    commonTest {
      dependencies {
        implementation(kotlin("test-common"))
        implementation(kotlin("test-annotations-common"))
      }
    }
    jvmTest {
      dependencies {
        implementation(kotlin("test-junit5"))
      }
    }
  }

  targets.configureEach {
    compilations.configureEach {
      compileTaskProvider.get().compilerOptions {
        // https://youtrack.jetbrains.com/issue/KT-61573
        freeCompilerArgs.add("-Xexpect-actual-classes")
      }
    }
  }
}

mavenPublishing {
  publishToMavenCentral()

  signAllPublications()

  coordinates(group.toString(), project.name, version.toString())

  pom {
    name = "aws-lambda-kotlin-runtime"
    description = "Kotlin runtime for AWS Lambda with Kotlin/Native and JVM/GraalVM support."
    url = "https://github.com/asyncant/aws-lambda-kotlin-runtime"
    licenses {
      license {
        name = "MIT"
        distribution = "repo"
      }
    }
    developers {
      developer {
        id = "asyncant"
        name = "asyncant"
        url = "http://www.asyncant.com"
      }
    }
    scm {
      connection = "scm:git:git://github.com/asyncant/aws-lambda-kotlin-runtime.git"
      developerConnection = "scm:git:ssh://github.com/asyncant/aws-lambda-kotlin-runtime.git"
      url = "https://github.com/asyncant/aws-lambda-kotlin-runtime"
    }
  }
}

signing {
  useGpgCmd()
  sign(publishing.publications)
}

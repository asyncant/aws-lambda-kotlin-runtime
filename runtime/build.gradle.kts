import com.vanniktech.maven.publish.SonatypeHost

plugins {
  kotlin("multiplatform")
  id("org.jetbrains.dokka") version "1.6.10"
  jacoco
  id("com.vanniktech.maven.publish") version "0.32.0"
  id("signing")
}
group = rootProject.group
version = rootProject.version

kotlin {
  linuxX64()
  linuxArm64()

  sourceSets {
    commonTest {
      dependencies {
        implementation(kotlin("test-common"))
        implementation(kotlin("test-annotations-common"))
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

val javadocJar by tasks.registering(Jar::class) {
  dependsOn(tasks.dokkaHtml)
  archiveClassifier.set("javadoc")
  from(tasks.dokkaHtml.get().outputDirectory)
}

mavenPublishing {
  publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)

  signAllPublications()

  coordinates(group.toString(), project.name, version.toString())

  pom {
    name = "aws-lambda-kotlin-runtime"
    description = "Kotlin/Native runtime for AWS Lambda."
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

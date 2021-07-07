plugins {
  kotlin("multiplatform") version "1.5.0" apply false
  kotlin("plugin.serialization") version "1.5.0" apply false
}
group = "com.asyncant.aws.lambda"
version = "0.1-SNAPSHOT"

allprojects {
  repositories {
    mavenLocal()
    mavenCentral()
  }

  tasks.withType<Test> {
    useJUnitPlatform()
  }
}


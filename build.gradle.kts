plugins {
  kotlin("multiplatform") version "1.5.31" apply false
}
group = "com.asyncant.aws.lambda"
version = "0.8.1-SNAPSHOT"

allprojects {
  repositories {
    mavenLocal()
    mavenCentral()
  }

  tasks.withType<Test> {
    useJUnitPlatform()
  }
}


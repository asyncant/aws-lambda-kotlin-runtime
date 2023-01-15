plugins {
  kotlin("multiplatform") version "1.8.0" apply false
}
group = "com.asyncant.aws.lambda"
version = "0.8.2-SNAPSHOT"

allprojects {
  tasks.withType<Test> {
    useJUnitPlatform()
  }
}


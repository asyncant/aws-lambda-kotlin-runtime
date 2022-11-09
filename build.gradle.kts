plugins {
  kotlin("multiplatform") version "1.7.20" apply false
}
group = "com.asyncant.aws.lambda"
version = "0.8.2-SNAPSHOT"

allprojects {
  tasks.withType<Test> {
    useJUnitPlatform()
  }
}


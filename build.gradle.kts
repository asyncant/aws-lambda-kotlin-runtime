plugins {
  kotlin("multiplatform") version "2.1.21" apply false
}
group = "com.asyncant.aws.lambda"
version = "0.8.3-SNAPSHOT"

allprojects {
  tasks.withType<Test> {
    useJUnitPlatform()
  }
}

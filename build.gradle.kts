plugins {
  kotlin("multiplatform") version "2.1.21" apply false
}
group = "com.asyncant.aws.lambda"
version = "0.9.0"

allprojects {
  tasks.withType<Test> {
    useJUnitPlatform()
  }
}

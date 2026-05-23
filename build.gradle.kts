plugins {
  kotlin("multiplatform") version "2.3.21" apply false
}
group = "com.asyncant.aws.lambda"
version = "0.9.6"

allprojects {
  tasks.withType<Test> {
    useJUnitPlatform()
  }
}

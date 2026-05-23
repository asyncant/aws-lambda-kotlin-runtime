plugins {
  kotlin("multiplatform") version "2.3.0" apply false
}
group = "com.asyncant.aws.lambda"
version = "0.9.5"

allprojects {
  tasks.withType<Test> {
    useJUnitPlatform()
  }
}

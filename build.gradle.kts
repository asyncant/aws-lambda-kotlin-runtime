plugins {
  kotlin("multiplatform") version "1.6.10" apply false
}
group = "com.asyncant.aws.lambda"
version = "0.8.1"

allprojects {
  tasks.withType<Test> {
    useJUnitPlatform()
  }
}


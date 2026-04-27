plugins {
  kotlin("jvm")
  application
  id("org.graalvm.buildtools.native") version "0.10.6"
}
group = "com.asyncant.samples"
version = "0.0.123"

dependencies {
  implementation(project(":runtime"))
}

application {
  mainClass.set("MainKt")
}

graalvmNative {
  binaries {
    named("main") {
      imageName.set("bootstrap")
      mainClass.set("MainKt")
      buildArgs.add("--no-fallback")
    }
  }
}

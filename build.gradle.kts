plugins {
  id("idea")
  id("java-library")
  id("maven-publish")

  kotlin("jvm") version libs.versions.kotlin
}

group = "uk.tvidal"
version = "1.0-SNAPSHOT"

repositories {
  mavenCentral()
}

dependencies {
  api(libs.slf4j)
  compileOnly(libs.jackson)
  compileOnly(libs.mariadb)
  compileOnly(libs.postgresql)
  implementation(kotlin("reflect"))

  testImplementation(kotlin("test"))
  testRuntimeOnly(libs.jackson.kotlin)
  testRuntimeOnly(libs.logback)
  testRuntimeOnly(libs.mariadb)
  testRuntimeOnly(libs.postgresql)
}

tasks.test {
  useJUnitPlatform()
}

kotlin {
  jvmToolchain(21)
}

java {
  withSourcesJar()
  withJavadocJar()
}

publishing {
  publications {
    create<MavenPublication>("jar") {
      from(components["java"])
    }
  }
}

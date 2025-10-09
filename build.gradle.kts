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
  api(libs.jpa)
  api(libs.slf4j)
  compileOnly(libs.h2)
  compileOnly(libs.jackson)
  compileOnly(libs.mariadb)
  compileOnly(libs.postgresql)
  implementation(kotlin("reflect"))

  testImplementation(kotlin("test"))
  testImplementation(libs.jackson.kotlin)
  testRuntimeOnly(libs.logback)
  testRuntimeOnly(libs.h2)
  testRuntimeOnly(libs.mariadb)
  testRuntimeOnly(libs.postgresql)
  testApi(libs.assertj)
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

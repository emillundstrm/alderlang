plugins {
    java
    kotlin("jvm") version "1.5.10"
}

group = "com.github.emillundstrm.alderlang"
version = "0.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("com.github.h0tk3y.betterParse:better-parse:0.4.2")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
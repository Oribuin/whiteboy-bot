import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("java-library")
    id("application")
    id("com.gradleup.shadow") version ("8.3.5")
}

group = "oribuin.dev"
version = "1.0-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17

    disableAutoTargetJvm()
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenCentral()
    gradlePluginPortal()

    maven("https://jitpack.io")
}

dependencies {
    api("net.dv8tion:JDA:6.4.1")
    api("org.incendo:cloud-core:2.0.0")
    api("org.incendo:cloud-annotations:2.0.0")
    api("org.incendo:cloud-jda6:1.0.0-beta.4")

    // Utility
    api("org.apache.commons:commons-lang3:3.12.0")
    api("org.reflections:reflections:0.10.2")
    api("com.google.code.gson:gson:2.10")
    api("com.google.guava:guava:32.1.1-jre")

    // SQL Libraries
    api("com.zaxxer:HikariCP:5.0.1")
    api("org.xerial:sqlite-jdbc:3.40.0.0")

}

tasks {
    compileJava {
        this.options.compilerArgs.add("-parameters")
        this.options.isFork = true
        this.options.encoding = "UTF-8"
    }
}


application {
    this.mainClass.set("dev.oribuin.whiteboy.WhiteBoyBot")
}


tasks.withType(ShadowJar::class.java) {
    this.mergeServiceFiles()
}

tasks.withType(GradleBuild::class.java) {
    this.dependsOn("shadowJar")
    this.dependsOn("copy")
}
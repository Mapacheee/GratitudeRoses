import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("java")
    id("java-library")
    id("com.gradleup.shadow") version "8.3.2"
}

group = "me.mapacheee.gratituderoses"
version = "1.0.0"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
    maven("https://repo.extendedclip.com/releases/")
    maven("https://maven.enginehub.org/repo/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")

    compileOnlyApi("com.thewinterframework:wintercore:1.0.0")
    annotationProcessor("com.thewinterframework:wintercore:1.0.0")

    //api("com.thewinterframework:paper:1.0.4")
    //annotationProcessor("com.thewinterframework:paper:1.0.4")
    //api("com.thewinterframework:configuration:1.0.2")
    //annotationProcessor("com.thewinterframework:configuration:1.0.2")
    //api("com.thewinterframework:command:1.0.1")
    //annotationProcessor("com.thewinterframework:command:1.0.1")
    compileOnly("me.clip:placeholderapi:2.11.6")
    compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.0.14")
    implementation("net.kyori:adventure-text-serializer-legacy:4.17.0")
    implementation("com.zaxxer:HikariCP:7.0.2")
}

/*tasks.named<ShadowJar>("shadowJar") {
    exclude("com/thewinterframework/**")
}*/
